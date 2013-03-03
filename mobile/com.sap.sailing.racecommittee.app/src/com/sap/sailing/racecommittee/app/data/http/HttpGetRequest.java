package com.sap.sailing.racecommittee.app.data.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

public class HttpGetRequest extends HttpRequest {

    public HttpGetRequest(URI requestUri) throws MalformedURLException, IOException {
        super(requestUri.toURL());
    }

    @Override
    protected InputStream execute(HttpURLConnection connection) throws IOException {
        return new BufferedInputStream(connection.getInputStream());
    }
}
