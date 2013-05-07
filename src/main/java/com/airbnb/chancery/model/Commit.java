package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {
	@JsonProperty
	private String url;
	@JsonProperty
	private String timestamp;
	@JsonProperty
	private Entity author;
	@JsonProperty
	private Entity committer;
	@JsonProperty
	private boolean distinct;
	@NotNull
	@JsonProperty
	private String id;
	@JsonProperty
	private String message;
	@JsonProperty
	private List<String> added;
	@JsonProperty
	private List<String> modified;
	@JsonProperty
	private List<String> removed;
}
