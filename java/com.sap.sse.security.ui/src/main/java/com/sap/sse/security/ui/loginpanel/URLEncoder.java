package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.http.client.URL;

public class URLEncoder {
    
    private URLEncoder() { }

    private final static char[] charsToReplace = { '\'', '(', ')', '"', '[', ']', '{', '}', '<', '>', '|' };
    
    public static String encode(String url) {
        String nearlyEncoded = URL.encode(url);
        for (char c : charsToReplace) {
            nearlyEncoded = nearlyEncoded.replaceAll("\\" + Character.toString(c), "%" + Integer.toHexString(c));
        }
        return nearlyEncoded;
    }
    
}
