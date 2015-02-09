package com.sap.sse.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLEncoder {
    /**
     * Use different encoding strategies, depending on whether running in GWT or server context.
     */
    public static String encode(String decodedUrlString) {
        try {
            Class.forName("com.google.gwt.http.client.URL");
            // GWT context
            return com.google.gwt.http.client.URL.encode(decodedUrlString);
        } catch (ClassNotFoundException e) {
            // non-GWT context (URL/URI should be fully available)
            try {
                URL url = new URL(decodedUrlString);
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                        url.getQuery(), url.getRef());
                return uri.toString();
            } catch (MalformedURLException | URISyntaxException f) {
                f.printStackTrace();
                return null;
            }
        }
    }
}
