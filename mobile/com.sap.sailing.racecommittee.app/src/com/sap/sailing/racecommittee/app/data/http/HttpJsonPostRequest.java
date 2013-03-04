package com.sap.sailing.racecommittee.app.data.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;

public class HttpJsonPostRequest extends HttpRequest {
    public final static String ContentType = "application/json;charset=UTF-8";

    private String requestBody;

    public HttpJsonPostRequest(URI requestUri, String body) throws MalformedURLException, IOException {
        super(requestUri.toURL());
        this.requestBody = body;
    }

    @Override
    protected InputStream execute(HttpURLConnection connection) throws IOException {
        // connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(0);

        connection.setRequestProperty("Content-Type", ContentType);
        connection.setRequestProperty("Accept", ContentType);

        OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        sendBody(outputStream);
        outputStream.close();

        return new BufferedInputStream(connection.getInputStream());
    }

    private void sendBody(OutputStream outputStream) throws IOException {
        outputStream.write(requestBody.getBytes(Charset.forName("UTF-8")));
    }
}
