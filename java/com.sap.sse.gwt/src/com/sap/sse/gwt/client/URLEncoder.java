package com.sap.sse.gwt.client;

import com.google.gwt.http.client.URL;

/**
 * A very basic URL encoder that also works in a GWT environment where <code>java.net.URLEncoder</code> is not available.
 * It is based on {@link URL#encode(String)} and adds encoding for the backslash (\) character.
 * 
 * @author Axel Uhl (D043530)
 *
 */
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
