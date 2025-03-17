package com.sap.sse.security;

import java.util.logging.Logger;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.realm.Realm;

import com.sap.sse.security.shared.impl.User;

public class AtLeastOneSuccessfulStrategyWithLockingAndBanning extends AtLeastOneSuccessfulStrategy {
    private static final Logger logger = Logger
            .getLogger(AtLeastOneSuccessfulStrategyWithLockingAndBanning.class.getName());
    @Override
    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo,
            AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
        if (token != null && token.getPrincipal() != null && realm instanceof UsernamePasswordRealm) {
            final UsernamePasswordRealm upRealm = (UsernamePasswordRealm) realm;
            final String username = token.getPrincipal().toString();
            final User user = upRealm.getUserStore().getUserByName(username);
            if (user != null) {
                if (t != null) {
                    if (t instanceof IncorrectCredentialsException) {
                        logger.info("failed password authentication for user "+username);
                        user.getLockingAndBanning().failedPasswordAuthentication();
                    }
                } else {
                    // no exception, so the authentication must have been successful
                    user.getLockingAndBanning().successfulPasswordAuthentication();
                }
            }
        }
        return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }

}
