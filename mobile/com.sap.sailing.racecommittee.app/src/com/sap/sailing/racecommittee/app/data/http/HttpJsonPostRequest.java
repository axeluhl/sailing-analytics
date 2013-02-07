package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

public class HttpJsonPostRequest extends HttpRequest {
	public final static String JsonUTF8 = "application/json;charset=UTF-8";

	public HttpJsonPostRequest(URI requestUri, String body) throws UnsupportedEncodingException {
		super(createPostRequest(requestUri, body));
	}
	
	private static HttpPost createPostRequest(URI requestUri, String body) throws UnsupportedEncodingException {
		StringEntity entity = new StringEntity(body);
		entity.setContentType(JsonUTF8);
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, JsonUTF8));

		HttpPost post = new HttpPost(requestUri);
		post.setEntity(entity);
		return post;
	}

	@Override
	protected InputStream processResponse(HttpResponse response) throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();
		return entity.getContent();
	}
}
