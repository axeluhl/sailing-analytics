package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;

public class DeleteHttpRequest extends HttpRequest {

	public HttpResponse delete(String service) throws ClientProtocolException, IOException {
		HttpDelete delete = new HttpDelete(service);
		HttpResponse response = executeRequest(delete, "DELETE request");
		return response;
	}
}
