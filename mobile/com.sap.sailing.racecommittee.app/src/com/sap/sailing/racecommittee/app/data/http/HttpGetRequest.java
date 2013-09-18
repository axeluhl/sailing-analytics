package com.sap.sailing.racecommittee.app.data.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest extends HttpRequest {

    public HttpGetRequest(URL requestUrl) {
        super(requestUrl);
    }

    @Override
    protected BufferedInputStream execute(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getInputStream();
        return new BufferedInputStream(stream);
    }
}
