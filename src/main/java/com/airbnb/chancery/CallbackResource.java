package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Path("/callback")
@RequiredArgsConstructor
public class CallbackResource {
    private final AmazonS3Client s3Client;
    private final String bucket;
    private final GithubClient ghClient;
    private final String challenge;
    private final ObjectKeyEvaluator objectKeyEvaluator;
    private final UpdateFilter updateFilter;
    private final ObjectMapper mapper = new ObjectMapper();

    @Timed
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveHook(@QueryParam("challenge") String attempt,
                                @FormParam("payload") String raw) throws IOException {
        log.debug("Received {}", raw);

        final CallbackPayload payload = mapper.readValue(raw, CallbackPayload.class);
        log.debug("Decoded to {}", payload);

        if (challenge == null) {
            log.debug("Empty challenge");
        } else if (!challenge.equals(attempt)) {
            log.warn("Challenge attempt {} doesn't match {}, ditching request", attempt, challenge);
            return Response.status(Response.Status.FORBIDDEN).entity("failed challenge").build();

        } else {
            log.info("Passed challenge");
        }

        if (updateFilter.shouldUpdate(payload)) {
            if (payload.isDeleted())
                delete(payload);
            else
                update(payload);
        } else {
            log.info("Not updating");
        }

        return Response.ok().build();
    }

    private void update(CallbackPayload payload) throws IOException {
        final String key = objectKeyEvaluator.getPath(payload);

        log.info("Downloading for {}", key);

        final java.nio.file.Path path;

        try {
            path = ghClient.download(
                    payload.getRepository().getOwner().getName(),
                    payload.getRepository().getName(),
                    payload.getHeadCommit().getId());
        } catch (IOException e) {
            log.error("Couldn't download for {}", key, e);
            throw e;
        }

        log.info("Uploading to {}", key);

        try {
            s3Client.putObject(bucket, key, path.toFile());
        } catch (Exception e) {
            log.error("Couldn't upload to {}", key, e);
            throw e;
        }

        log.info("Uploaded to {}", key);

        try {
            Files.delete(path);
        } catch (IOException e) {
            log.warn("Couldn't delete {}", path, e);
        }
    }

    private void delete(CallbackPayload payload) {
        log.info("Reference deleted, removing corresponding object");
        final String key = objectKeyEvaluator.getPath(payload);

        try {
            s3Client.deleteObject(bucket, key);
        } catch (Exception e) {
            log.error("Couldn't delete {}", key, e);
            throw e;
        }

        log.info("Deleted {}", key);
    }
}
