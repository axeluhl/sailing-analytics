package com.sap.sailing.server.gateway.subscription;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.AuthenticatingRealm;

/**
 * Shiro Realm for handling subscription webhooks basic authentication
 * 
 * @author tutran
 */
public class SubscriptionWebhookBasicAuthRealm extends AuthenticatingRealm {
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken authToken = (UsernamePasswordToken) token;

        WebhookBasicAuthConfiguration authConfiguration = WebhookBasicAuthConfiguration.getInstance();
        
        if (authToken.getUsername() != null && authToken.getPassword() != null
                && authToken.getUsername().equals(authConfiguration.getUsername())
                && new String(authToken.getPassword()).equals(authConfiguration.getPassword())) {
            return new SimpleAuthenticationInfo(authToken.getPrincipal(), authToken.getCredentials(),
                    "SAP");
        }

        return null;
    }

}
