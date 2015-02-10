package com.sap.sse.util.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sap.sse.common.util.UrlHelper;

public enum NonGwtUrlHelper implements UrlHelper {
    INSTANCE;
    
    public String encodeUrl(String decodedUrlString) {
        try {
            URL url = new URL(decodedUrlString);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());
            return uri.toString();
        } catch (MalformedURLException | URISyntaxException f) {
            return null;
        }
    }
}
