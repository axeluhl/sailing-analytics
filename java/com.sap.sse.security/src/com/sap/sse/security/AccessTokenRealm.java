package com.sap.sse.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;

/**
 * A realm that authenticates users by an access token which has previously been obtained through
 * {@link SecurityService#createAccessToken(String)}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AccessTokenRealm extends AbstractUserStoreBasedRealm {
    
    public AccessTokenRealm() {
        super();
    }
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof AccessToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        AccessToken accessToken = (AccessToken) token;
        final User user = getUserStore().getUserByAccessToken(accessToken.getCredentials());
        if (user == null) {
            return null;
        }
        // return salted credentials
        SaltedAuthenticationInfo sai = new SimpleSaltedAuthenticationInfo(user.getName(), accessToken.getCredentials(), /* salt */ null);
        return sai;
    }

}
