package com.sap.sailing.gwt.ui.client;

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
