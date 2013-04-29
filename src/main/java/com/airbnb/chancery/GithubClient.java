package com.airbnb.chancery;

import com.airbnb.chancery.model.RateLimitStats;
import com.airbnb.chancery.model.ReferenceCreationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
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

@Slf4j
public class GithubClient {
    @NotNull
    private final WebResource resource;
    @NotNull
    private final ObjectMapper mapper = new ObjectMapper();

    GithubClient(final @NotNull Client client, final @Nullable String oAuth2Token) {
        client.setFollowRedirects(true);

        resource = client.resource("https://api.github.com/");

        resource.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                /* Github API *requires* this */
                cr.getHeaders().putSingle("User-Agent", "chancery by pierre@gcarrier.fr");
                return getNext().handle(cr);
            }
        });

        if (oAuth2Token != null && !oAuth2Token.isEmpty()) {
            final String authValue = "token " + oAuth2Token;
            resource.addFilter(new ClientFilter() {
                @Override
                public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                    cr.getHeaders().putSingle("Authorization", authValue);
                    return getNext().handle(cr);
                }
            });
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

        try {
            /* Github wants a Content-Length, and Jersey doesn't fancy doing that */
            final byte[] payload = mapper.writeValueAsBytes(req);

            resource.uri(uri).
                    type(MediaType.APPLICATION_JSON_TYPE).
                    post(payload);
        } catch (JsonProcessingException | UniformInterfaceException e) {
            throw new GithubFailure.forReferenceCreation(e);
        }
    }

    public Path download(String owner, String repository, String id)
            throws IOException, GithubFailure.forDownload {
        final Path tempPath = Files.createTempFile("com.airbnb.chancery", null);
        tempPath.toFile().deleteOnExit();

        final URI uri = UriBuilder.
                fromPath("/repos/{a}/{b}/tarball/{c}").
                build(owner, repository, id);

        log.info("Downloading {}", uri);

        try {
            final InputStream inputStream = resource.uri(uri).
                    accept(MediaType.WILDCARD_TYPE).
                    get(InputStream.class);

            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Downloaded {}", uri);
            return tempPath;
        } catch (UniformInterfaceException e) {
            throw new GithubFailure.forDownload(e);
        }
    }
}
