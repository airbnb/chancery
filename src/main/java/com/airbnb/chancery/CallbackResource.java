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

    @POST
    @Metered
    @ExceptionMetered
    @Timed(name = "receiveHook")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveHook(@HeaderParam("X-Hub-Signature") String signature,
                                String payload)
            throws IOException, GithubFailure.forDownload {
        log.debug("Received {}", payload);

        final CallbackPayload decodedPayload = mapper.readValue(payload, CallbackPayload.class);
        log.debug("Decoded to {}", decodedPayload);

        decodedPayload.setTimestamp(new DateTime());

        if (checker != null) {
            final boolean authenticated = checker.checkSignature(signature, payload);

            if (!authenticated)
                return Response.
                        status(Response.Status.FORBIDDEN).
                        build();
        }

        callbackBus.post(decodedPayload);

        return Response.
                status(Response.Status.ACCEPTED).
                build();
    }
}
