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
            // TODO See bug3664 => https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=3664
            // Because of changes in the query string encoding, the Sail InSight-App for iOS will now have problems
            // while parsing the registration URLs, which are sent to invited competitors, if e.g. the leaderboard name
            // contains whitespaces. To avoid this problems, we use this temporary fix until a new version of the Sail
            // InSight-App for iOS is rolled out. Afterwards this fix must be replaced by the line below. 
            return URLEncoder.encode(queryString, "UTF-8").replace("+", "%20");
            // return URLEncoder.encode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "problem encoding URL query string "+queryString, e);
            return null;
        }
    }
}
