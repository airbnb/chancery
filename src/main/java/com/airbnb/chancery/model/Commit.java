package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {
    @JsonProperty
    private /* final */ String url;
    @JsonProperty
    private /* final */ String timestamp;
    @JsonProperty
    private /* final */ Entity author;
    @JsonProperty
    private /* final */ Entity committer;
    @JsonProperty
    private /* final */ boolean distinct;
    @NotNull
    @JsonProperty
    private /* final */ String id;
    @JsonProperty
    private /* final */ String message;
    @JsonProperty
    private /* final */ List<String> added;
    @JsonProperty
    private /* final */ List<String> modified;
    @JsonProperty
    private /* final */ List<String> removed;
}
