package com.sap.sse.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalCookie extends SimpleCookie {
    
    public GlobalCookie(String name) {
        super(name);
    }
    
    public GlobalCookie() {
        super();
    }
    
    public GlobalCookie(Cookie template) {
        super(template);
    }

    Logger log = LoggerFactory.getLogger(GlobalCookie.class);

    @Override
    public String getPath() {
        return ROOT_PATH;
    }

    @Override
    public void setPath(String path) {
    }

    @Override
    public void saveTo(HttpServletRequest request, HttpServletResponse response) {
        String name = getName();
        String value = getValue();
        String comment = getComment();
        String domain = getDomain();
        String path = ROOT_PATH;
        int maxAge = getMaxAge();
        int version = getVersion();
        boolean secure = isSecure();
        boolean httpOnly = isHttpOnly();

        addCookieHeader(response, name, value, comment, domain, path, maxAge, version, secure, httpOnly);
    }

    @Override
    public void removeFrom(HttpServletRequest request, HttpServletResponse response) {
        String name = getName();
        String value = DELETED_COOKIE_VALUE;
        String comment = null; //don't need to add extra size to the response - comments are irrelevant for deletions
        String domain = getDomain();
        String path = ROOT_PATH;
        int maxAge = 0; //always zero for deletion
        int version = getVersion();
        boolean secure = isSecure();
        boolean httpOnly = false; //no need to add the extra text, plus the value 'deleteMe' is not sensitive at all

        addCookieHeader(response, name, value, comment, domain, path, maxAge, version, secure, httpOnly);

        log.trace("Removed '{}' cookie by setting maxAge=0", name);
    }

    private void addCookieHeader(HttpServletResponse response, String name, String value, String comment,
            String domain, String path, int maxAge, int version, boolean secure, boolean httpOnly) {

        String headerValue = buildHeaderValue(name, value, comment, domain, path, maxAge, version, secure, httpOnly);
        response.addHeader(COOKIE_HEADER_NAME, headerValue);

        if (log.isDebugEnabled()) {
            log.debug("Added HttpServletResponse Cookie [{}]", headerValue);
        }
    }
}
