package com.airbnb.chancery;

import com.amazonaws.services.s3.AmazonS3Client;
import com.yammer.metrics.core.HealthCheck;

public class S3ClientHealthCheck extends HealthCheck {
    private final AmazonS3Client client;
    private final String bucket;

    protected S3ClientHealthCheck(AmazonS3Client client, String bucket) {
        super("s3: " + bucket);
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    protected Result check() throws Exception {
        if (!client.doesBucketExist(bucket))
            return Result.unhealthy("Bucket %s is reported non-existent", bucket);
        return Result.healthy();
    }
}
