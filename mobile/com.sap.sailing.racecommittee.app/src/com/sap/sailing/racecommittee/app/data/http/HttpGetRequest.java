package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

public class HttpGetRequest extends HttpRequest {


	public HttpGetRequest(URI requestUri) {
		super(new HttpGet(requestUri));
	}

	@Override
	protected InputStream processResponse(HttpResponse response) throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();
		return entity.getContent();
	}
}
