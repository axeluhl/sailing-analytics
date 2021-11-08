package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

public class HttpJsonGetRequest extends HttpRequest {
    public final static String ContentType = "application/json;charset=UTF-8";

    public HttpJsonGetRequest(Context context, URL requestUrl) {
        super(context, requestUrl);
    }

    @Override
    protected BufferedInputStream doRequest(HttpURLConnection connection) throws IOException {
        // connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setChunkedStreamingMode(0);

        connection.setRequestProperty("Content-Type", ContentType);
        connection.setRequestProperty("Accept", ContentType);

        // OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        // outputStream.close();

        return new BufferedInputStream(connection.getInputStream());
    }
}
