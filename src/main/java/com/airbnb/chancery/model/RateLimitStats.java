package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RateLimitStats {
    @JsonProperty
    int limit;
    @JsonProperty
    int remaining;

    @Data
    public static class Container {
        @JsonProperty
        RateLimitStats rate;
    }
}
