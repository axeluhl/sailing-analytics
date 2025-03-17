package com.sap.sse.security;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.realm.Realm;

import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.util.ServiceTrackerFactory;

public class AtLeastOneSuccessfulStrategyWithLockingAndBanning extends AtLeastOneSuccessfulStrategy {
    private static final Logger logger = Logger.getLogger(AtLeastOneSuccessfulStrategyWithLockingAndBanning.class.getName());
    
    private final Future<SecurityService> securityService;
    
    public AtLeastOneSuccessfulStrategyWithLockingAndBanning() {
        if (Activator.getContext() != null) {
            securityService = ServiceTrackerFactory.createServiceFuture(Activator.getContext(), SecurityService.class);
        } else {
            securityService = null;
        }
    }
    
    private SecurityService getSecurityService() {
        SecurityService result;
        try {
            result = securityService == null ? null : securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error retrieving security service", e);
            result = null;
        }
        return result;
    }

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
                        final SecurityService mySecurityService = getSecurityService();
                        if (mySecurityService != null) {
                            mySecurityService.failedPasswordAuthentication(user);
                        } else {
                            logger.warning("Account locking due to failed password authentication for user "+username+" not possible; security service not found");
                        }
                    }
                } else {
                    // no exception, so the authentication must have been successful
                    final SecurityService mySecurityService = getSecurityService();
                    if (mySecurityService != null) {
                        mySecurityService.successfulPasswordAuthentication(user);
                    }
                }
            }
        }
        return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
    }
}
