package com.airbnb.chancery.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class Entity {
    @JsonProperty
    private /* final */ String name;
    @JsonProperty
    private /* final */ String username;
    @JsonProperty
    private /* final */ String email;
}
