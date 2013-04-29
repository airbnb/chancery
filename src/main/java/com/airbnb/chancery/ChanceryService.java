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
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
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

        final GithubClient ghClient = new GithubClient(
                buildGithubHttpClient(config, env),
                config.getGithubOauth2Token()
        );

        final String githubSecret = config.getGithubSecret();
        final GithubAuthChecker ghAuthChecker =
                (githubSecret == null) ? null :
                        new GithubAuthChecker(githubSecret);

        env.addHealthCheck(new GithubClientHealthCheck(ghClient));

        final List<RefLoggerConfig> refLoggerConfigs = config.getRefLogs();
        if (refLoggerConfigs != null)
            for (RefLoggerConfig refLoggerConfig : refLoggerConfigs) {
                log.info("Creating ref logger for {}", refLoggerConfig);
                final RefLogger refLogger = new RefLogger(refLoggerConfig, ghClient);
                callbackBus.register(refLogger);
            }

        final List<S3ArchiverConfig> s3ArchiverConfigs = config.getS3Archives();
        if (s3ArchiverConfigs != null) {
            final AmazonS3Client s3Client = buildS3Client(config);
            final HashSet<String> buckets = new HashSet<>();

            for (S3ArchiverConfig s3ArchiverConfig : s3ArchiverConfigs) {
                log.info("Creating S3 archiver for {}", s3ArchiverConfig);
                callbackBus.register(new S3Archiver(s3ArchiverConfig, s3Client, ghClient));
                buckets.add(s3ArchiverConfig.getBucketName());
            }

            for (String bucketName : buckets)
                env.addHealthCheck(new S3ClientHealthCheck(s3Client, bucketName));
        }

        final CallbackResource resource = new CallbackResource(ghAuthChecker, callbackBus);
        env.addResource(resource);
    }
}
