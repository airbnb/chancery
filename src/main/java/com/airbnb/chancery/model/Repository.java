package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
	@JsonProperty
	private int id;
	@JsonProperty
	private int watchers;
	@JsonProperty
	private int stargazers;
	@JsonProperty
	private String url;
	@JsonProperty
	private int size;
	@JsonProperty("has_wiki")
	private boolean wiki;
	@JsonProperty("has_issues")
	private boolean issues;
	@JsonProperty("has_downloads")
	private boolean downloads;
	@JsonProperty
	private int forks;
	@JsonProperty
	private boolean fork;
	@JsonProperty
	private String description;
	@JsonProperty("created_at")
	private int createdAt;
	@JsonProperty
	private String language;
	@JsonProperty("master_branch")
	private String masterBranch;
	@JsonProperty
	private String name;
	@JsonProperty("open_issues")
	private int openIssues;
	@JsonProperty
	private String organization;
	@JsonProperty("private")
	private boolean privateRepo;
	@JsonProperty
	private int pushed_at;
	@JsonProperty
	private Entity owner;
	@JsonProperty
	private String homepage;
}
