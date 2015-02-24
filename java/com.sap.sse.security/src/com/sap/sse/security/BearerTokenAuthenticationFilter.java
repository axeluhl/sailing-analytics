package com.sap.sse.security;


import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;

/**
 * Looks for an "Authorization: Bearer &lt;token&gt;" HTTP header. If found, tries to authenticate a user
 * with the bearer token presented. If instead an "Authorization: Basic ..." header field is found, HTTP
 * basic authentication is attempted using a regular username/password token.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class BearerTokenAuthenticationFilter extends BasicHttpAuthenticationFilter {
    
    private static final String BEARER = "Bearer";

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            String[] authTokens = authorizationHeader.split(" ");
            if (authTokens[0].equals(BEARER)) {
                return new BearerAuthenticationToken(authTokens[1]);
            }
        }
        return super.createToken(request, response);
    }

    @Override
    protected boolean isLoginAttempt(String authzHeader) {
        String authzScheme = BEARER.toLowerCase(Locale.ENGLISH);
        return authzHeader.toLowerCase(Locale.ENGLISH).startsWith(authzScheme) || super.isLoginAttempt(authzHeader);
    }
}
