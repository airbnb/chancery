package com.airbnb.chancery;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import java.util.concurrent.Executors;

public class ChanceryService extends Service<ChanceryConfig> {
    public static void main(String[] args) throws Exception {
        new ChanceryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ChanceryConfig> bootstrap) {
        bootstrap.setName("chancery");
    }

    private EventBus buildCallbackBus(final ChanceryConfig config) {

        return new AsyncEventBus(
                Executors.newFixedThreadPool(config.getHandlerThreads())
        );
    }

    private Client buildGithubHttpClient(final ChanceryConfig config,
                                         final Environment env) {
        return new JerseyClientBuilder().
                using(config.getGithubHttpConfig()).
                using(env).build();
    }

    private AmazonS3Client buildS3Client(final ChanceryConfig config) {
        return new AmazonS3Client(new BasicAWSCredentials(
                config.getAwsAccessKeyID(),
                config.getAwsSecretKey()
        ));
    }

    @Override
    public void run(final ChanceryConfig config, final Environment env)
            throws Exception {
        final EventBus callbackBus = buildCallbackBus(config);


        final PayloadExpressionEvaluator objectKeyEvaluator =
                new PayloadExpressionEvaluator(config.getObjectPathTemplate());

        final UpdateFilter filter =
                new UpdateFilter(config.getRepoRefPattern());

        final Client githubHttpClient = buildGithubHttpClient(config, env);
        final GithubClient ghClient =
                new GithubClient(githubHttpClient, config.getGithubOauth2Token());

        final String githubSecret = config.getGithubSecret();
        final GithubAuthChecker ghAuthChecker =
                (githubSecret == null) ? null :
                        new GithubAuthChecker(githubSecret);


        final String s3Bucket = config.getBucketName();

        env.addHealthCheck(new S3ClientHealthCheck(s3Client, s3Bucket));
        env.addHealthCheck(new GithubClientHealthCheck(ghClient));

        final CallbackResource resource = new CallbackResource(ghAuthChecker, callbackBus);

        env.addResource(resource);
    }
}
