package com.sap.sailing.server.gateway.subscription.chargebee;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.AuthenticatingRealm;

/**
 * Shiro Realm for handling subscription WebHook basic authentication
 * 
 * @author Tu Tran
 */
public class PaymentSystemWebHookAuthRealm extends AuthenticatingRealm {
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        final AuthenticationInfo result;
        final UsernamePasswordToken authToken = (UsernamePasswordToken) token;
        final WebHookBasicAuthConfiguration authConfiguration = WebHookBasicAuthConfiguration.getInstance();
        if (authToken.getUsername() != null && authToken.getPassword() != null
                && authToken.getUsername().equals(authConfiguration.getUsername())
                && new String(authToken.getPassword()).equals(authConfiguration.getPassword())) {
            result = new SimpleAuthenticationInfo(authToken.getPrincipal(), authToken.getCredentials(), getClass().getSimpleName());
        } else {
            result = null;
        }
        return result;
    }
}
