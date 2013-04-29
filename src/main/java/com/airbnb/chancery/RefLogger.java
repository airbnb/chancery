package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.airbnb.chancery.model.Repository;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Slf4j
public class RefLogger extends FilteringSubscriber {
    private final PayloadExpressionEvaluator refTemplate;
    private final GithubClient ghClient;

    public RefLogger(RefLoggerConfig config, GithubClient ghClient) {
        super(config.getRefFilter());
        this.ghClient = ghClient;
        refTemplate = new PayloadExpressionEvaluator(config.getRefTemplate());
    }

    @Override
    protected void handleCallback(@NotNull CallbackPayload callbackPayload) throws Exception {
        if (callbackPayload.isDeleted())
            return;

        final String ref = refTemplate.evaluateForPayload(callbackPayload);
        final Repository repo = callbackPayload.getRepository();
        final String hash = callbackPayload.getAfter();
        final String owner = repo.getOwner().getName();
        final String repoName = repo.getName();

        log.info("Creating ref {} to {} in {}/{}", ref, hash, owner, repoName);
        ghClient.createReference(owner, repoName, ref, hash);
        log.info("Created ref {} to {} in {}/{}", ref, hash, owner, repoName);
    }
}
