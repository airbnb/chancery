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
	private Repository repository;
	@JsonProperty
	@NotNull
	private String ref;
	@JsonProperty
	private Entity pusher;
	@JsonProperty
	@NotNull
	private String after;
	@JsonProperty
	private String before;
	@JsonProperty
	private String compare;
	@JsonProperty
	private boolean created;
	@JsonProperty
	private boolean deleted;
	@JsonProperty
	private boolean forced;
	@JsonProperty("head_commit")
	private Commit headCommit;
	@JsonProperty
	private List<Commit> commits;
	@JsonIgnore
	private DateTime timestamp;
}
