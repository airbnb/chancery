package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallbackPayload {
    @JsonProperty
    @NotNull
    private /* final */ Repository repository;
    @JsonProperty
    @NotNull
    private /* final */ String ref;
    @JsonProperty
    private /* final */ Entity pusher;
    @JsonProperty
    @NotNull
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
    @JsonIgnore
    private /* final */ DateTime timestamp;
}
