package com.airbnb.chancery;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubRateLimitData {
    @JsonProperty
    int limit;
    @JsonProperty
    int remaining;

    @Data
    static class Container {
        @JsonProperty
        GithubRateLimitData rate;
    }
}
