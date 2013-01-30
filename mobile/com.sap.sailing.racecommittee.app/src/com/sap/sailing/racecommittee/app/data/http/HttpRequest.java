package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sap.sailing.racecommittee.app.logging.ExLog;

public abstract class HttpRequest {
	
	private final static String TAG = HttpRequest.class.getName();
	
	private final static int lowestOkCode = HttpStatus.SC_OK;
	private final static int lowestRedirectCode = HttpStatus.SC_MULTIPLE_CHOICES; 
	
	private static void validateHttpResponse(HttpResponse response) {
		StatusLine status = response.getStatusLine();
		if (status != null) {
			int statusCode = status.getStatusCode();
			if (statusCode >= lowestOkCode && statusCode < lowestRedirectCode) {
				return;
			}
			throw new IllegalStateException(String.format("Request response had error code %d.", statusCode));
		}
		throw new IllegalStateException(String.format("Request response had no valid status."));
	}
	
	protected final static String JsonUTF8 = "application/json;charset=UTF-8";
	
	protected HttpClient client;
	
	public HttpRequest() {
		client = new DefaultHttpClient();
	}
	
	protected HttpResponse executeRequest(HttpRequestBase request, String content) throws IOException, ClientProtocolException {
		ExLog.i(TAG, "Requesting " + request.getMethod() +  " " + request.getURI().toString());
		HttpResponse response = client.execute(request);
		validateHttpResponse(response);
		ExLog.i(TAG, "Response " + response.getStatusLine().toString() + " for " + content);
		return response;
	}
	

	
}
