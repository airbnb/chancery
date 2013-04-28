package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.validation.ValidationMethod;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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
    @Getter
    private String githubSecret;

    /* S3-related */
    @Nullable
    @JsonProperty
    @Getter
    private String awsAccessKeyID;

    @Nullable
    @JsonProperty
    @Getter
    private String awsSecretKey;

    /* Handlers */
    @Nullable
    @JsonProperty
    @Getter
    private List<S3ArchiverConfig> s3Archives;

    @Nullable
    @JsonProperty
    @Getter
    private List<RefLoggerConfig> refLogs;

    @Valid
    @NotNull
    @JsonProperty
    @Getter
    private JerseyClientConfiguration githubHttpConfig =
            new JerseyClientConfiguration();

    @ValidationMethod(message = "missing S3 credentials")
    public boolean isProvidingS3Credentials() {
        return (s3Archives == null ||
                (awsAccessKeyID != null && awsSecretKey != null));
    }
}
