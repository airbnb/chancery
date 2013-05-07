package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceCreationRequest {
    @JsonProperty("ref")
    private String name;
    @JsonProperty
    private String sha;
}
