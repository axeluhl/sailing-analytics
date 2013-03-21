package com.sap.sailing.domain.persistence.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MongoUtils {
    /**
     * Key names in MongoDB must not start with a $ sign and must not contain a "." character. This method escapes
     * those. {@link #unescapeDollarAndDot} is the inverse function.
     * 
     * @param key
     *            a non-<code>null</code> string which may have length 0
     * @return a string that can be used as a key in a MongoDB key-value pair and that can be decoded again using
     *         {@link #unescapeDollarAndDot(String)}.
     */
    public static String escapeDollarAndDot(String key) {
        String result = key.replace("%", "%25").replace("+", "%2B");
        if (result.length() > 0 && result.charAt(0) == '$') {
            result = "%24"+result.substring(1);
        }
        return result.replace(".", "%2E");
    }

    /**
     * @see #escapeDollarAndDot(String)
     * @param key
     *            a non-<code>null</code> string which may have length 0 and that was returned by
     *            {@link #escapeDollarAndDot(String)}.
     * @return the string originally passed to {@link #escapeDollarAndDot(String)}
     */
    public static String unescapeDollarAndDot(String escapedKey) {
        try {
            return URLDecoder.decode(escapedKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("How come this VM doesn't know UTF-8?");
        }
    }

}
