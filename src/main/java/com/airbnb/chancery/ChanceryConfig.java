package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.HttpClientConfiguration;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ChanceryConfig extends Configuration {
    @NotEmpty
    @JsonProperty
    @Getter
    private String awsAccessKeyID;

    @NotEmpty
    @JsonProperty
    @Getter
    private String awsSecretKey;

    @NotEmpty
    @JsonProperty
    @Getter
    private String githubOauth2Token;

    @JsonProperty
    @Getter
    private String uriChallenge;

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
