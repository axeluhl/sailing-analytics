package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.content.Context;
import android.util.Log;

public class HttpJsonPostRequest extends HttpRequest {
    private final static String TAG = HttpJsonPostRequest.class.getName();
    public final static String ContentType = "application/json;charset=UTF-8";

    private String requestBody;

    public HttpJsonPostRequest(URL requestUrl, Context context) {
        this(requestUrl, null, context);
    }
    
    public HttpJsonPostRequest(URL requestUrl, String body, Context context) {
        super(requestUrl, context);
        this.requestBody = body;
    }

    @Override
    protected BufferedInputStream doRequest(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(0);

        connection.setRequestProperty("Content-Type", ContentType);
        connection.setRequestProperty("Accept", ContentType);
        OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        try {
            sendBody(outputStream);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception trying to close HTTP stream", e);
                }
            }
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
