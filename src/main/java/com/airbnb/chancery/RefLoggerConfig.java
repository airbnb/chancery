package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class RefLoggerConfig extends Configuration {
    @NotEmpty
    @JsonProperty
    private String refFilter;

    @NotEmpty
    @JsonProperty
    private String refTemplate;
}
