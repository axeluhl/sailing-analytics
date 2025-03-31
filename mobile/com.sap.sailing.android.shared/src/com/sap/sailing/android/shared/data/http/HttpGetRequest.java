package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

public class HttpGetRequest extends HttpRequest {

    public HttpGetRequest(URL requestUrl, Context context) {
        super(context, requestUrl);
    }

    public HttpGetRequest(URL url, HttpRequestProgressListener listener, Context context) {
        super(context, url, listener);
    }

    @Override
    protected BufferedInputStream doRequest(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getInputStream();
        return new BufferedInputStream(stream);
    }
}
