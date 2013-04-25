package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CallbackPayload {
    @JsonProperty
    private /* final */ Repository repository;
    @JsonProperty
    private /* final */ String ref;
    @JsonProperty
    private /* final */ Entity pusher;
    @JsonProperty
    private /* final */ String after;
    @JsonProperty
    private /* final */ String before;
    @JsonProperty
    private /* final */ String compare;
    @JsonProperty
    private /* final */ boolean created;
    @JsonProperty
    private /* final */ boolean deleted;
    @JsonProperty
    private /* final */ boolean forced;
    @JsonProperty("head_commit")
    private /* final */ Commit headCommit;
    @JsonProperty
    private /* final */ List<Commit> commits;
}
