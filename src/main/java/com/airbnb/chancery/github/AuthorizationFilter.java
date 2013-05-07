package com.airbnb.chancery.github;

import lombok.RequiredArgsConstructor;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

@RequiredArgsConstructor
final class AuthorizationFilter extends ClientFilter {
	private final String authValue;

	@Override
	public ClientResponse handle(ClientRequest cr)
			throws ClientHandlerException {
		cr.getHeaders().putSingle("Authorization", authValue);
		return getNext().handle(cr);
	}
}
