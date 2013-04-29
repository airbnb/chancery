package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/* TODO: some metrics */
@Slf4j
public class S3Archiver {
    @NonNull
    private final RefFilter refFilter;
    @NonNull
    private final AmazonS3Client s3Client;
    @NonNull
    private final String bucketName;
    @NonNull
    private final GithubClient ghClient;
    @NonNull
    private final PayloadExpressionEvaluator keyTemplate;

    public S3Archiver(@NotNull S3ArchiverConfig config,
                      @NotNull AmazonS3Client s3Client,
                      @NotNull GithubClient ghClient) {
        this.s3Client = s3Client;
        this.ghClient = ghClient;
        bucketName = config.getBucketName();
        refFilter = new RefFilter(config.getRefFilter());
        keyTemplate = new PayloadExpressionEvaluator(config.getKeyTemplate());
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receiveCallback(@NotNull CallbackPayload payload)
            throws IOException, GithubFailure.forDownload {
        if (!refFilter.matches(payload))
            return;

        final String key = keyTemplate.evaluateForPayload(payload);

        if (payload.isDeleted())
            delete(key);
        else {
            final java.nio.file.Path path;

            final String hash = payload.getAfter();
            final String owner = payload.getRepository().getOwner().getName();
            final String repoName = payload.getRepository().getName();

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
        try {
            s3Client.deleteObject(bucketName, key);
        } catch (Exception e) {
            log.error("Couldn't delete {} from {}", key, bucketName, e);
            throw e;
        }

        log.info("Deleted {} from {}", key, bucketName);
    }

    private void upload(@NotNull File src, @NotNull String key) {
        log.info("Uploading {} to {} in {}", src, key, bucketName);
        try {
            s3Client.putObject(this.bucketName, key, src);
        } catch (Exception e) {
            log.error("Couldn't upload to {} in {}", key, bucketName, e);
            throw e;
        }
        log.info("Uploaded to {} in {}", key, bucketName);
    }
}
