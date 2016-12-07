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

    public HttpJsonPostRequest(Context context, URL requestUrl) {
        this(context, requestUrl, null, null);
    }

    public HttpJsonPostRequest(Context context, URL requestUrl, String body) {
        this(context, requestUrl, body, null);
    }

    public HttpJsonPostRequest(Context context, URL requestUrl, String body, String accessToken) {
        super(context, requestUrl);
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
