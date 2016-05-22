package com.sap.sse.util.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.util.UrlHelper;

public enum NonGwtUrlHelper implements UrlHelper {
    INSTANCE;
    
    private static final Logger logger = Logger.getLogger(NonGwtUrlHelper.class.getName());
    
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
