package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.airbnb.chancery.model.Repository;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

/* TODO: some metrics */
@Slf4j
public class RefLogger {
    private final RefFilter refFilter;
    private final PayloadExpressionEvaluator refTemplate;
    private final GithubClient ghClient;

    public RefLogger(RefLoggerConfig config, GithubClient ghClient) {
        this.ghClient = ghClient;
        refFilter = new RefFilter(config.getRefFilter());
        refTemplate = new PayloadExpressionEvaluator(config.getRefTemplate());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receiveCallback(@NotNull CallbackPayload payload)
            throws GithubFailure.forReferenceCreation {
        if (!refFilter.matches(payload))
            return;

        if (payload.isDeleted())
            return;

        final String ref = refTemplate.evaluateForPayload(payload);
        final Repository repo = payload.getRepository();
        final String hash = payload.getAfter();
        final String owner = repo.getOwner().getName();
        final String repoName = repo.getName();

        log.info("Creating ref {} to {} in {}/{}", ref, hash, owner, repoName);
        ghClient.createReference(owner, repoName, ref, hash);
    }
}
