package com.airbnb.chancery;

import com.yammer.metrics.core.HealthCheck;

public class GithubClientHealthCheck extends HealthCheck {
    private final GithubClient client;

    protected GithubClientHealthCheck(final GithubClient client) {
        super("github");
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        final GithubRateLimitData data = client.getRateLimitData();
        final float ratio = (float)data.getRemaining() / (float)data.getLimit();
        if (ratio < 0.5)
            return Result.unhealthy("Running low on API quota: %d/%d (%.2f%%) left",
                    data.getRemaining(),
                    data.getLimit(),
                    100.0*ratio);
        else
            return Result.healthy("Checked API quota & still high: %d/%d (%.2f%%) left",
                    data.getRemaining(),
                    data.getLimit(),
                    100.0 * ratio);
    }
}
