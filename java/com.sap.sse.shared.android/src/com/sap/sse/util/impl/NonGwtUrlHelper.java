package com.sap.sse.util.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.util.UrlHelper;

public enum NonGwtUrlHelper implements UrlHelper {
    INSTANCE;
    
    private static final Logger logger = Logger.getLogger(NonGwtUrlHelper.class.getName());
    
    public String encodeUrl(String decodedUrlString) {
        try {
            URL url = new URL(decodedUrlString);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());
            return uri.toString();
        } catch (MalformedURLException | URISyntaxException f) {
            logger.log(Level.WARNING, "problem encoding URL string "+decodedUrlString, f);
            return null;
        }
    }

    @Override
    public String encodeQueryString(String queryString) {
        try {
            return URLEncoder.encode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "problem encoding URL query string "+queryString, e);
            return null;
        }
    }
}
