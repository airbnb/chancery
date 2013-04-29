package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

public class S3ArchiverConfig extends Configuration {
    @Getter
    @NotEmpty
    @JsonProperty
    private String refFilter;

    @Getter
    @NotEmpty
    @JsonProperty
    private String bucketName;

    @Getter
    @NotEmpty
    @JsonProperty
    private String keyTemplate;
}
