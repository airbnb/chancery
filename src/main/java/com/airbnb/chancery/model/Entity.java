package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity {
    @JsonProperty
    private /* final */ String name;
    @JsonProperty
    private /* final */ String username;
    @JsonProperty
    private /* final */ String email;
}
