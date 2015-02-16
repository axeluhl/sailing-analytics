package com.sap.sse.security;

import org.apache.shiro.authc.AuthenticationToken;

import com.sap.sse.security.impl.Activator;

/**
 * An access token as issued by {@link SecurityService#createAccessToken(String)}. The user name which is returned
 * by {@link #getPrincipal()} is obtained by looking up the access token using the {@link SecurityService}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AccessToken implements AuthenticationToken {
    private static final long serialVersionUID = 8528031991813216585L;
    private final String accessToken;
    
    public AccessToken(String accessToken) {
        super();
        this.accessToken = accessToken;
    }

    @Override
    public Object getPrincipal() {
        SecurityService securityService = Activator.getSecurityService();
        User user = securityService.getUserByAccessToken(accessToken);
        return user == null ? null : user.getName();
    }

    @Override
    public String getCredentials() {
        return accessToken;
    }

}
