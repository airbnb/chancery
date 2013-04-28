package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ChanceryConfig extends Configuration {
    /* Needed */
    @NotEmpty
    @JsonProperty
    @Getter
    private int handlerThreads = 16;

    @NotEmpty
    @JsonProperty
    @Getter
    private String githubOauth2Token;

    /* Optional */
    @JsonProperty
    private Getter githubSecret;

    /* S3-related */
    @JsonProperty
    @Getter
    private String awsAccessKeyID;
    @JsonProperty
    @Getter
    private String awsSecretKey;

    /* TODO: move */
    @NotEmpty
    @JsonProperty
    @Getter
    private String bucketName;
    @NotEmpty
    @JsonProperty
    @Getter
    private String objectPathTemplate;
    @NotEmpty
    @JsonProperty
    @Getter
    private String repoRefPattern;
    @Valid
    @NotNull
    @JsonProperty
    @Getter
    private JerseyClientConfiguration githubHttpConfig = new JerseyClientConfiguration();
}
