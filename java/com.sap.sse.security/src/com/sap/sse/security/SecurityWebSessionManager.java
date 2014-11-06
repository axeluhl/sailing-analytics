package com.sap.sse.security;

import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

public class SecurityWebSessionManager extends DefaultWebSessionManager {
    public SecurityWebSessionManager() {
        super();
        getSessionIdCookie().setPath(Cookie.ROOT_PATH);
    }
}
