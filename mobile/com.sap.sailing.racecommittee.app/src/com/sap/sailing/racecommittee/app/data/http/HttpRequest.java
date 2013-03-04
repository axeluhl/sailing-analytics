package com.sap.sailing.racecommittee.app.data.http;

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

    private HttpURLConnection connection;

    public HttpRequest(URL url) throws IOException {
        this((HttpURLConnection) url.openConnection());
    }

    public HttpRequest(HttpURLConnection connection) {
        this.connection = connection;
    }

    protected abstract InputStream execute(HttpURLConnection connection) throws IOException;

    public InputStream execute() throws Exception {
        ExLog.i(TAG, String.format("Executing HTTP request on %s.", connection.getURL()));

        connection.setConnectTimeout(5000);

        InputStream stream = execute(connection);
        validateHttpResponse(connection);
        return stream;
    }

    public void disconnect() {
        connection.disconnect();
    }

}
