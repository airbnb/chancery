package com.airbnb.chancery;

import com.sun.jersey.api.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;

@Slf4j
public class StupidRateLimitTest {
    @Test
    public void testRateLimit() throws URISyntaxException {
        final GithubClient client = new GithubClient(new Client(), null);
        final GithubRateLimitData data = client.getRateLimitData();
        StupidRateLimitTest.log.info("Rate limiting data: {}", data);
        Assert.assertTrue(data.getLimit() > 10);
        Assert.assertTrue(data.getRemaining() > 10);
        Assert.assertTrue(data.getRemaining() <= data.getLimit());
    }
}
