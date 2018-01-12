package com.sap.sse.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sse.common.Duration;

public class HttpUrlConnectionHelper {
    private static final int HTTP_MAX_REDIRECTS = 5;

    /**
     * Redirects the connection using the <code>Location</code> header. Make sure to set
     * the timeout if you expect the response to take longer.
     */
    public static HttpURLConnection redirectConnection(URL url, Duration timeout) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        URL nextUrl = url;
        for (int counterOfRedirects = 0; counterOfRedirects <= HTTP_MAX_REDIRECTS; counterOfRedirects++) {
            if (connection != null) {
                connection.disconnect();
            }
            connection = (HttpURLConnection) nextUrl.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0...");
            connection.setDoOutput(true);
            connection.setReadTimeout((int) timeout.asMillis());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                    || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                nextUrl = new URL(nextUrl, location);
            } else {
                break;
            }
        }
        return connection;
    }
    
    public static HttpURLConnection redirectConnection(URL url) throws MalformedURLException, IOException {
    	return redirectConnection(url, Duration.ONE_MINUTE.times(10));
    }
}
