package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.validation.ValidationMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
public class ChanceryConfig extends Configuration {
    /* Needed */
    @JsonProperty
    private int handlerThreads = 16;

    @NotEmpty
    @JsonProperty
    private String githubOauth2Token;

    /* Optional */
    @Nullable
    @JsonProperty
    private String githubSecret;

    /* S3-related */
    @Nullable
    @JsonProperty
    private String awsAccessKeyID;

    @Nullable
    @JsonProperty
    private String awsSecretKey;

    /* Handlers */
    @Nullable
    @JsonProperty
    private List<S3ArchiverConfig> s3Archives;

    @Nullable
    @JsonProperty
    private List<RefLoggerConfig> refLogs;

    @Valid
    @JsonProperty
    private JerseyClientConfiguration githubHttpConfig =
            new JerseyClientConfiguration();

    @ValidationMethod(message = "missing S3 credentials")
    public boolean isProvidingS3Credentials() {
        return (s3Archives == null ||
                (awsAccessKeyID != null && awsSecretKey != null));
    }
}
