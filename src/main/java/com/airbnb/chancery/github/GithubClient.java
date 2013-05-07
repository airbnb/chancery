package com.airbnb.chancery.github;

import com.airbnb.chancery.GithubFailure;
import com.airbnb.chancery.model.RateLimitStats;
import com.airbnb.chancery.model.ReferenceCreationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.*;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class GithubClient {
    	@NotNull
    private final WebResource resource;
    @NotNull
    private final ObjectMapper mapper = new ObjectMapper();
    @NonNull
    private final Timer downloadTimer = Metrics.newTimer(getClass(), "download",
            TimeUnit.SECONDS, TimeUnit.SECONDS);
    @NonNull
    private final Timer referenceCreationTimer = Metrics.newTimer(getClass(), "create-reference",
            TimeUnit.SECONDS, TimeUnit.SECONDS);

    public GithubClient(final @NotNull Client client, final @Nullable String oAuth2Token) {
        client.setFollowRedirects(true);

        resource = client.resource("https://api.github.com/");

        resource.addFilter(new UserAgentFilter());

        if (oAuth2Token != null && !oAuth2Token.isEmpty()) {
            final String authValue = "token " + oAuth2Token;
            resource.addFilter(new AuthorizationFilter(authValue));
        } else {
            GithubClient.log.warn("No Github oAuth2 token provided");
        }
    }

    public RateLimitStats getRateLimitData()
            throws GithubFailure.forRateLimit {
        try {
            return resource.
                    uri(new URI("/rate_limit")).
                    accept(MediaType.APPLICATION_JSON_TYPE).
                    get(RateLimitStats.Container.class).getRate();
        } catch (URISyntaxException e) {
            return null; /* mkay? */
        } catch (UniformInterfaceException e) {
            throw new GithubFailure.forRateLimit(e);
        }
    }

    public void createReference(String owner, String repository, String ref, String id)
            throws GithubFailure.forReferenceCreation {
        final URI uri = UriBuilder.
                fromPath("/repos/{a}/{b}/git/refs").
                build(owner, repository);

        final ReferenceCreationRequest req = new ReferenceCreationRequest(ref, id);

        final TimerContext time = referenceCreationTimer.time();
        try {
            /* Github wants a Content-Length, and Jersey doesn't fancy doing that */
            final byte[] payload = mapper.writeValueAsBytes(req);

            resource.uri(uri).
                    type(MediaType.APPLICATION_JSON_TYPE).
                    post(payload);
        } catch (JsonProcessingException | UniformInterfaceException e) {
            throw new GithubFailure.forReferenceCreation(e);
        } finally {
            time.stop();
        }
    }

    public Path download(String owner, String repository, String id)
            throws IOException, GithubFailure.forDownload {
        final Path tempPath = Files.createTempFile("com.airbnb.chancery-githubdownload-", null);
        tempPath.toFile().deleteOnExit();

        final URI uri = UriBuilder.
                fromPath("/repos/{a}/{b}/tarball/{c}").
                build(owner, repository, id);

        log.info("Downloading {}", uri);

        final TimerContext time = downloadTimer.time();
        try {
            final InputStream inputStream = resource.uri(uri).
                    accept(MediaType.WILDCARD_TYPE).
                    get(InputStream.class);

            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Downloaded {}", uri);
            return tempPath;
        } catch (UniformInterfaceException e) {
            throw new GithubFailure.forDownload(e);
        } finally {
            time.stop();
        }
    }
}
