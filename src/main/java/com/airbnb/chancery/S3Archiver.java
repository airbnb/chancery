package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.amazonaws.services.s3.AmazonS3Client;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
public class S3Archiver extends FilteringSubscriber {
    @NonNull
    private final AmazonS3Client s3Client;
    @NonNull
    private final String bucketName;
    @NonNull
    private final GithubClient ghClient;
    @NonNull
    private final PayloadExpressionEvaluator keyTemplate;
    @NonNull
    private final Timer uploadTimer = Metrics.newTimer(getClass(), "upload",
            TimeUnit.SECONDS, TimeUnit.SECONDS);
    @NonNull
    private final Timer deleteTimer = Metrics.newTimer(getClass(), "delete",
            TimeUnit.SECONDS, TimeUnit.SECONDS);

    public S3Archiver(@NotNull S3ArchiverConfig config,
                      @NotNull AmazonS3Client s3Client,
                      @NotNull GithubClient ghClient) {
        super(config.getRefFilter());
        this.s3Client = s3Client;
        this.ghClient = ghClient;
        bucketName = config.getBucketName();
        keyTemplate = new PayloadExpressionEvaluator(config.getKeyTemplate());
    }

    @Override
    protected void handleCallback(@NotNull CallbackPayload callbackPayload) throws Exception {
        final String key = keyTemplate.evaluateForPayload(callbackPayload);

        if (callbackPayload.isDeleted())
            delete(key);
        else {
            final Path path;

            final String hash = callbackPayload.getAfter();
            final String owner = callbackPayload.getRepository().getOwner().getName();
            final String repoName = callbackPayload.getRepository().getName();


            path = ghClient.download(owner, repoName, hash);
            upload(path.toFile(), key);

            try {
                Files.delete(path);
            } catch (IOException e) {
                log.warn("Couldn't delete {}", path, e);
            }
        }
    }

    private void delete(@NotNull String key) {
        log.info("Removing key {} from {}", key, bucketName);
        final TimerContext time = deleteTimer.time();
        try {
            s3Client.deleteObject(bucketName, key);
        } catch (Exception e) {
            log.error("Couldn't delete {} from {}", key, bucketName, e);
            throw e;
        } finally {
            time.stop();
        }

        log.info("Deleted {} from {}", key, bucketName);
    }

    private void upload(@NotNull File src, @NotNull String key) {
        log.info("Uploading {} to {} in {}", src, key, bucketName);
        final TimerContext time = uploadTimer.time();
        try {
            s3Client.putObject(this.bucketName, key, src);
        } catch (Exception e) {
            log.error("Couldn't upload to {} in {}", key, bucketName, e);
            throw e;
        } finally {
            uploadTimer.stop();
        }
        log.info("Uploaded to {} in {}", key, bucketName);
    }
}
