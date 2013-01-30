package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

/**
 * @author Basil Hess
 * 
 * Used for POST Request to a REST Service
 * 
 * 'requestObjectAsString' is POSTed
 */
public class PostHttpRequest extends HttpRequest {
	
	public HttpResponse post(String requestObjectAsString, String service) throws ClientProtocolException, IOException {
		String jsonString = requestObjectAsString;
		StringEntity entity = new StringEntity(jsonString);
		entity.setContentType(JsonUTF8);
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, JsonUTF8));

		HttpPost post = new HttpPost(service);
		post.setEntity(entity);
		HttpResponse response = executeRequest(post, requestObjectAsString);
		return response;
	}
}
