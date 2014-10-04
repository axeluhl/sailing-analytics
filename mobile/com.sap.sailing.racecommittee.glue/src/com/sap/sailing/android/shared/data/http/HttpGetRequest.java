package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest extends HttpRequest {

    public HttpGetRequest(URL requestUrl) {
        super(requestUrl);
    }

    public HttpGetRequest(URL url, HttpRequestProgressListener listener) {
        super(url, listener);
    }



    @Override
    protected BufferedInputStream doRequest(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getInputStream();
        return new BufferedInputStream(stream);
    }
}
