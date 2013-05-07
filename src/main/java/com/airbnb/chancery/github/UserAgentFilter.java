package com.airbnb.chancery.github;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

final class UserAgentFilter extends ClientFilter {
	@Override
	public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
	    /* Github API *requires* this */
	    cr.getHeaders().putSingle("User-Agent", "chancery by pierre@gcarrier.fr");
	    return getNext().handle(cr);
	}
}