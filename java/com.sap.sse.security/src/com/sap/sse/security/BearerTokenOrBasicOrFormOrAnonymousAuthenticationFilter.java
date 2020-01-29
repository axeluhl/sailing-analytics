package com.sap.sse.security;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import com.sap.sse.security.jaxrs.api.SecurityResource;

/**
 * Looks for an "Authorization: Bearer &lt;token&gt;" HTTP header. If found, tries to authenticate a user
 * with the bearer token presented. If instead an "Authorization: Basic ..." header field is found, HTTP
 * basic authentication is attempted using a regular username/password token. If that isn't found, looks
 * for username/password fields in the POST parameters, like the {@link FormAuthenticationFilter} does.<p>
 * 
 * This filter can be used for securing REST requests, in conjunction with the {@link SecurityResource}'s
 * {@link SecurityResource#accessToken(String, String)} through which a bearer token can be obtained using
 * basic authentication.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilter extends BearerTokenOrBasicOrFormAuthenticationFilter {
    public BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilter() {
        super(/* application name */ "SAP");
    }
    
    protected BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilter(String applicationName) {
        super(applicationName);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        super.onAccessDenied(request, response);
        return true; // The request should always be processed, in case of doubt anonymously.
    }
    
    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        return false; // Do not build a challenge for authorization in case of an anonymous user.
    }
}
