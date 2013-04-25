package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Path("/callback")
@RequiredArgsConstructor
public class CallbackResource {
    private final AmazonS3Client s3Client;
    private final String bucket;
    private final GithubClient ghClient;
    private final ObjectKeyEvaluator objectKeyEvaluator;
    private final UpdateFilter updateFilter;
    private final ObjectMapper mapper = new ObjectMapper();

    @Timed
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String receiveHook(@FormParam("payload") String raw) throws IOException {
        log.trace("Received {}", raw);
        final CallbackPayload payload = mapper.readValue(raw, CallbackPayload.class);

        log.trace("Decoded to {}", payload);
        if (updateFilter.shouldUpdate(payload)) {
            if (payload.isDeleted())
                delete(payload);
            else
                update(payload);
        } else {
            log.info("Not updating");
        }

        return "OK";
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
