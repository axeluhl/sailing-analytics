package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

public class GetHttpRequest extends HttpRequest {

	public InputStream get(URI requestUri) throws IOException {
		HttpRequestBase get = new HttpGet(requestUri);
		HttpResponse response = executeRequest(get);
		HttpEntity entity = response.getEntity();
		return entity.getContent();
	}
}
