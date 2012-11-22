package com.sap.sailing.gwt.ui.client;

import com.google.gwt.http.client.URL;

public class URLFactoryImpl implements URLFactory {

    private final static char[] charsToReplace = { '\'', '(', ')', '"', '[', ']', '{', '}', '<', '>', '|' };
    
    @Override
    public String encode(String url) {
        String nearlyEncoded = URL.encode(url);
        for (char c : charsToReplace) {
            nearlyEncoded = nearlyEncoded.replaceAll("\\" + Character.toString(c), "%" + Integer.toHexString(c));
        }
        return nearlyEncoded;
    }

}
