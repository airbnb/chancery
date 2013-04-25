package com.airbnb.chancery;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.yammer.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

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
    private final WebResource resource;

    GithubClient(final Client client, final String oAuth2Token) {
        client.setFollowRedirects(true);

        resource = client.resource("https://api.github.com/");

        resource.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                cr.getHeaders().putSingle("User-Agent", "chancery by pierre@gcarrier.fr");
                return getNext().handle(cr);
            }
        });

        if (oAuth2Token != null && !oAuth2Token.isEmpty()) {
            resource.addFilter(new ClientFilter() {
                private final String authValue = "token " + oAuth2Token;

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

    @Timed(name = "github-rate-limit-pull")
    public GithubRateLimitData getRateLimitData() {
        try {
            return resource.uri(new URI("/rate_limit")).
                    accept(MediaType.APPLICATION_JSON_TYPE).
                    get(GithubRateLimitData.Container.class).getRate();
        } catch (URISyntaxException e) {
            /* no really? */
            return null;
        }
    }

    @Timed(name = "github-download")
    public Path download(String owner, String repository, String id) throws IOException {
        final Path tempPath = Files.createTempFile("com.airbnb.chancery", null);
        tempPath.toFile().deleteOnExit();
        final InputStream inputStream = resource.uri(UriBuilder.fromPath("/repos/{a}/{b}/tarball/{c}").
                build(owner, repository, id)).accept(MediaType.WILDCARD_TYPE).get(InputStream.class);
        Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
        return tempPath;
    }
}
