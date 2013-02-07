package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sap.sailing.racecommittee.app.logging.ExLog;

public abstract class HttpRequest {
	
	private final static String TAG = HttpRequest.class.getName();
	
	private final static int lowestOkCode = HttpStatus.SC_OK;
	private final static int lowestRedirectCode = HttpStatus.SC_MULTIPLE_CHOICES; 
	
	private static void validateHttpResponse(HttpResponse response) throws IOException {
		StatusLine status = response.getStatusLine();
		if (status != null) {
			int statusCode = status.getStatusCode();
			if (statusCode >= lowestOkCode && statusCode < lowestRedirectCode) {
				return;
			}
			throw new IOException(String.format("Request response had error code %d.", statusCode));
		}
		throw new IOException(String.format("Request response had no valid status."));
	}
	
	private HttpClient client;
	private HttpRequestBase httpRequestBase;
	
	public HttpRequest(HttpRequestBase request) {
		this.httpRequestBase = request;
		this.client = new DefaultHttpClient();
	}
	protected abstract InputStream processResponse(HttpResponse response) throws Exception;

	public InputStream execute() throws Exception {
		ExLog.i(TAG, "Executing request " + httpRequestBase.getMethod() +  " on " + httpRequestBase.getURI());
		HttpResponse response = client.execute(httpRequestBase);
		validateHttpResponse(response);
		ExLog.i(TAG, "Received reseponse " + response.getStatusLine());
		return processResponse(response);
	}

	
	

	
}
