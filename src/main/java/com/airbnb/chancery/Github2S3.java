package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
public class Github2S3 {
    @NonNull
    private final UpdateFilter updateFilter;
    @NonNull
    private final AmazonS3Client s3Client;
    @NonNull
    private final String bucket;
    @NonNull
    private final GithubClient ghClient;
    @NonNull
    private final PayloadExpressionEvaluator objectKeyEvaluator;

    private void update(CallbackPayload payload)
            throws IOException, GithubFailure.forDownload {
        final String key = objectKeyEvaluator.evaluateForPayload(payload);

        if (!updateFilter.shouldUpdate(payload)) {
            log.info("No need to update");
            return;
        }

        if (payload.isDeleted())
            delete(key);
        else {
            final java.nio.file.Path path;

            path = ghClient.download(
                    payload.getRepository().getOwner().getName(),
                    payload.getRepository().getName(),
                    payload.getAfter());

            upload(path.toFile(), key);

            try {
                Files.delete(path);
            } catch (IOException e) {
                log.warn("Couldn't delete {}", path, e);
            }
        }
    }

    private void delete(String key) {
        log.info("Reference deleted, removing corresponding {}", key);
        try {
            s3Client.deleteObject(bucket, key);
        } catch (Exception e) {
            log.error("Couldn't delete {}", key, e);
            throw e;
        }

        log.info("Deleted {}", key);
    }

    private void upload(File src, String key) {
        log.info("Uploading {} to {}", src, key);
        try {
            s3Client.putObject(this.bucket, key, src);
        } catch (Exception e) {
            log.error("Couldn't upload to {}", key, e);
            throw e;
        }
        log.info("Uploaded to {}", key);
    }
}
