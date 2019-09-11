package com.sap.sailing.server.testsupport;

import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.util.ServiceTrackerFactory;

public class SecurityBundleTestWrapper {
    private static final Logger logger = Logger.getLogger(SecurityBundleTestWrapper.class.getName());
    
    public SecurityService initializeSecurityServiceForTesting() throws UserGroupManagementException, UserManagementException, InterruptedException {
        final SecurityService securityService;
        if (Activator.getContext() == null) {
            logger.info("Setup for TaggingServiceTest in a non-OSGi environment");
            final UserStoreImpl store = new UserStoreImpl("defaultTenant");
            final AccessControlStoreImpl accessControlStoreImpl = new AccessControlStoreImpl(store);
            Activator.setTestStores(store, accessControlStoreImpl);
            securityService = new SecurityServiceImpl(store, accessControlStoreImpl);
            SecurityUtils.setSecurityManager(securityService.getSecurityManager());
            Activator.setSecurityService(securityService);
        } else {
            logger.info("Creating dummy UserStoreImpl to trigger loading of userstore mongodb bundle");
            new UserStoreImpl("defaultTenant"); // only to trigger bundle loading and activation so that security service can find the bundle and its original user store
            logger.info("Setup for TaggingServiceTest in an OSGi environment");
            // TODO: This timeout of 2 minutes is just for debugging purposes and should not be used in production!
            securityService = ServiceTrackerFactory.createAndOpen(Activator.getContext(), SecurityService.class).waitForService(120 * 1000);
        }
        return securityService;
    }
}
