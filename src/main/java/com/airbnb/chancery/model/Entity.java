package com.airbnb.chancery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity {
	@JsonProperty
	private String name;
	@JsonProperty
	private String username;
	@JsonProperty
	private String email;
}
