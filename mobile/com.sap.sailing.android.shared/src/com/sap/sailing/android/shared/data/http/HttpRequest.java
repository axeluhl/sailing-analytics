package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;

import com.sap.sailing.android.shared.logging.ExLog;

public abstract class HttpRequest {

    private final static String TAG = HttpRequest.class.getName();

    private final static int lowestOkCode = HttpStatus.SC_OK;
    private final static int lowestRedirectCode = HttpStatus.SC_MULTIPLE_CHOICES;

    private static void validateHttpResponseCode(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        if (statusCode != -1) {
            if (statusCode >= lowestOkCode && statusCode < lowestRedirectCode) {
                return;
            }
            throw new IOException(String.format("Request response had error code %d.", statusCode));
        }
        throw new IOException(String.format("Request response had no valid status."));
    }

    public interface HttpRequestProgressListener {
        void onHttpProgress(float progress);
    }

    private final HttpRequestProgressListener listener;
    private final URL url;
    private final Context context;
    private boolean isCancelled;

    public HttpRequest(URL url, Context context) {
        this(url, null, context);
    }

    public HttpRequest(URL url, HttpRequestProgressListener listener, Context context) {
        this.url = url;
        this.listener = listener;
        this.isCancelled = false;
        this.context = context;
    }

    public String getUrlAsString() {
        return url == null ? null : url.toString();
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    /**
     * Returns a copied {@link InputStream} of the server's response.
     * You must close this stream when done.
     */
    public InputStream execute() throws IOException {
        ExLog.i(context, TAG, String.format("(Request %d) Executing HTTP request on %s.", this.hashCode(), url));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("connection", "close");

        BufferedInputStream responseInputStream = null;
        try {
            try {
                responseInputStream = doRequest(connection);
            } catch (FileNotFoundException fnfe) {
                // 404 errors...
                throw new FileNotFoundException(String.format(
                        "(Request %d) %s\nHTTP response code: %d.\nHTTP response body: %s.", this.hashCode(),
                        fnfe.getMessage(), connection.getResponseCode(), connection.getResponseMessage()));
            }

            validateHttpResponseCode(connection);

            InputStream copiedResponseInputStream = readAndCopyResponse(connection, responseInputStream);
            
            if (copiedResponseInputStream != null) {
                ExLog.i(context, TAG, String.format("(Request %d) HTTP request executed.", this.hashCode()));
            } else {
                ExLog.i(context, TAG, String.format("(Request %d) HTTP request aborted.", this.hashCode()));
            }
            
            connection.disconnect();
            return copiedResponseInputStream;
        } catch (IOException e) {
            ExLog.i(context, TAG, String.format("(Request %d) HTTP request failed.", this.hashCode()));
            throw e;
        } finally {
            if (responseInputStream != null) {
                responseInputStream.close();
            }
        }
    }

    protected InputStream readAndCopyResponse(HttpURLConnection connection, BufferedInputStream inputStream)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (!readResponse(connection, inputStream, outputStream)) {
            return null;
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    protected boolean readResponse(HttpURLConnection connection, BufferedInputStream inputStream,
            OutputStream outputStream) throws IOException {
        int fileLength = connection.getContentLength();
        byte data[] = new byte[4096];
        long total = 0;
        int count;
        while ((count = inputStream.read(data)) != -1) {
            if (isCancelled()) {
                return false;
            }
            total += count;
            // publishing the progress....
            if (fileLength > 0 && listener != null) {
                listener.onHttpProgress(total / (float) fileLength);
            }
            outputStream.write(data, 0, count);
        }
        return true;
    }

    protected abstract BufferedInputStream doRequest(HttpURLConnection connection) throws IOException;

}
