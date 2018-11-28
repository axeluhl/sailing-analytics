package com.sap.sse.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SaltedAuthenticationInfo;

import com.sap.sse.security.shared.User;

/**
 * A realm that authenticates users by an access token which has previously been obtained through
 * {@link SecurityService#createAccessToken(String)}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class BearerTokenRealm extends AbstractCompositeAuthorizingRealm {
    
    public BearerTokenRealm() {
        super();
        setAuthenticationTokenClass(BearerAuthenticationToken.class);
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        BearerAuthenticationToken accessToken = (BearerAuthenticationToken) token;
        final User user = getUserStore().getUserByAccessToken(accessToken.getCredentials());
        if (user == null) {
            return null;
        }
        // return salted credentials
        SaltedAuthenticationInfo sai = new SimpleSaltedAuthenticationInfo(user.getName(), accessToken.getCredentials(), /* salt */ null);
        return sai;
    }
}
