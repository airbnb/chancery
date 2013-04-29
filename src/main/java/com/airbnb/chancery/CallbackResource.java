package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Slf4j
@Path("/callback")
@RequiredArgsConstructor
public class CallbackResource {
    @Nullable
    private final GithubAuthChecker checker;
    @NonNull
    private final EventBus callbackBus;
    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionMetered
    @Metered
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveHook(@HeaderParam("X-Hub-Signature") String signature,
                                String payload)
            throws IOException, GithubFailure.forDownload {
        CallbackResource.log.debug("Received {}", payload);

        CallbackPayload decodedPayload;

        try {
            decodedPayload = mapper.readValue(payload, CallbackPayload.class);
        } catch (Exception e) {
            log.warn("Couldn't parse payload {}", payload, e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }

        log.debug("Decoded to {}", decodedPayload);

        decodedPayload.setTimestamp(new DateTime());

        if (checker != null && !checker.checkSignature(signature, payload))
            return Response.
                    status(Response.Status.FORBIDDEN).
                    build();
        else
            callbackBus.post(decodedPayload);

        return Response.status(Response.Status.ACCEPTED).build();
    }
}
