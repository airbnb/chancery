package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Repository {
    @JsonProperty
    private /* final */ int id;
    @JsonProperty
    private /* final */ int watchers;
    @JsonProperty
    private /* final */ int stargazers;
    @JsonProperty
    private /* final */ String url;
    @JsonProperty
    private /* final */ int size;
    @JsonProperty("has_wiki")
    private /* final */ boolean wiki;
    @JsonProperty("has_issues")
    private /* final */ boolean issues;
    @JsonProperty("has_downloads")
    private /* final */ boolean downloads;
    @JsonProperty
    private /* final */ int forks;
    @JsonProperty
    private /* final */ boolean fork;
    @JsonProperty
    private /* final */ String description;
    @JsonProperty("created_at")
    private /* final */ int createdAt;
    @JsonProperty
    private /* final */ String language;
    @JsonProperty("master_branch")
    private /* final */ String masterBranch;
    @JsonProperty
    private /* final */ String name;
    @JsonProperty("open_issues")
    private /* final */ int openIssues;
    @JsonProperty
    private /* final */ String organization;
    @JsonProperty("private")
    private /* final */ boolean privateRepo;
    @JsonProperty
    private /* final */ int pushed_at;
    @JsonProperty
    private /* final */ Entity owner;
    @JsonProperty
    private /* final */ String homepage;
}
