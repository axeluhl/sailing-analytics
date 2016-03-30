package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.content.Context;

public class HttpJsonPostRequest extends HttpRequest {
    public final static String ContentType = "application/json;charset=UTF-8";

    private String requestBody;
    private String accessToken;

    public HttpJsonPostRequest(URL requestUrl, Context context) {
        this(requestUrl, null, context, null);
    }

    public HttpJsonPostRequest(URL requestUrl, String body, Context context) {
        this(requestUrl, body, context, null);
    }

    public HttpJsonPostRequest(URL requestUrl, String body, Context context, String accessToken) {
        super(requestUrl, context);
        this.requestBody = body;
        this.accessToken = accessToken;
    }

    @Override
    protected BufferedInputStream doRequest(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(0);

        connection.setRequestProperty("Content-Type", ContentType);
        connection.setRequestProperty("Accept", ContentType);
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
        OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        try {
            sendBody(outputStream);
        } finally {
            safeClose(outputStream);
        }
        return new BufferedInputStream(connection.getInputStream());
    }

    private void sendBody(OutputStream outputStream) throws IOException {
        if (requestBody != null) {
            outputStream.write(requestBody.getBytes(Charset.forName("UTF-8")));
        }
    }

    public String getRequestBody() {
        return requestBody;
    }
}
