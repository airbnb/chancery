package com.airbnb.chancery;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class ChanceryService extends Service<ChanceryConfig> {
    public static void main(String[] args) throws Exception {
        new ChanceryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ChanceryConfig> bootstrap) {
        bootstrap.setName("chancery");
    }

    @Override
    public void run(ChanceryConfig config, Environment env) throws Exception {
        final Client httpClient = new JerseyClientBuilder().
                using(config.getGithubHttpConfig()).
                using(env).build();

        final ObjectKeyEvaluator objectKeyEvaluator =
                new ObjectKeyEvaluator(config.getObjectPathTemplate());

        final UpdateFilter filter =
                new UpdateFilter(config.getRepoRefPattern());

        final GithubClient ghClient =
                new GithubClient(httpClient, config.getGithubOauth2Token());

        final BasicAWSCredentials awsCredentials =
                new BasicAWSCredentials(config.getAwsAccessKeyID(), config.getAwsSecretKey());
        final AmazonS3Client s3Client = new AmazonS3Client(awsCredentials);
        final String s3Bucket = config.getBucketName();

        env.addHealthCheck(new S3ClientHealthCheck(s3Client, s3Bucket));
        env.addHealthCheck(new GithubClientHealthCheck(ghClient));

        env.addResource(new CallbackResource(s3Client, s3Bucket, ghClient, objectKeyEvaluator, filter));
    }
}
