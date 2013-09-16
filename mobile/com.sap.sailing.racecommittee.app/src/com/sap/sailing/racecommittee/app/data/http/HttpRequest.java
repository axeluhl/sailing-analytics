package com.sap.sailing.racecommittee.app.data.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import com.sap.sailing.racecommittee.app.logging.ExLog;

public abstract class HttpRequest {

    private final static String TAG = HttpRequest.class.getName();

    private final static int lowestOkCode = HttpStatus.SC_OK;
    private final static int lowestRedirectCode = HttpStatus.SC_MULTIPLE_CHOICES;

    private static void validateHttpResponse(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        if (statusCode != -1) {
            if (statusCode >= lowestOkCode && statusCode < lowestRedirectCode) {
                return;
            }
            throw new IOException(String.format("Request response had error code %d.", statusCode));
        }
        throw new IOException(String.format("Request response had no valid status."));
    }

    private URL url;

    public HttpRequest(URL url) {
        this.url = url;
    }
    
    protected abstract BufferedInputStream execute(HttpURLConnection connection) throws IOException;

    public InputStream execute() throws Exception {
        ExLog.i(TAG, String.format("(Request %d) Executing HTTP request on %s.", this.hashCode(), url));
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("connection", "close");

        BufferedInputStream stream = null;
        try {
            stream = execute(connection);
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException(String.format("(Request %d) %s\nHTTP response code: %d.\nHTTP response body: %s.",
                    this.hashCode(), fnfe.getMessage(), connection.getResponseCode(), connection.getResponseMessage()));
        }

        validateHttpResponse(connection);
        byte[] streamData = copyStream(stream);
        connection.disconnect();
        
        ExLog.i(TAG, String.format("(Request %d) HTTP request executed.", this.hashCode()));
        return new ByteArrayInputStream(streamData);
    }

    private byte[] copyStream(BufferedInputStream stream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[4096];
        while ((len = stream.read(buffer)) != -1) {
          bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

}
