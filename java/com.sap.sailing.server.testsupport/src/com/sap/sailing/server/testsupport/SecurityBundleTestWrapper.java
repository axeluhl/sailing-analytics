package com.sap.sailing.server.testsupport;

import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;

import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityBundleTestWrapper {
    private static final Logger logger = Logger.getLogger(SecurityBundleTestWrapper.class.getName());
    
    public SecurityService initializeSecurityServiceForTesting() throws Exception {
        final SecurityService securityService;
        if (Activator.getContext() == null) {
            logger.info("Setup for TaggingServiceTest in a non-OSGi environment");
            final UserStoreImpl store = new UserStoreImpl("defaultTenant");
            store.ensureDefaultRolesExist();
            store.ensureServerGroupExists();
            final AccessControlStoreImpl accessControlStoreImpl = new AccessControlStoreImpl(store);
            Activator.setTestStores(store, accessControlStoreImpl);
            securityService = new SecurityServiceImpl(store, accessControlStoreImpl);
            ((SecurityServiceImpl) securityService).clearState();
            securityService.initialize();
            SecurityUtils.setSecurityManager(securityService.getSecurityManager());
            Activator.setSecurityService(securityService);
        } else {
            logger.info("Creating dummy UserStoreImpl to trigger loading of userstore mongodb bundle");
            new UserStoreImpl("defaultTenant"); // only to trigger bundle loading and activation so that security service can find the bundle and its original user store
            logger.info("Setup for TaggingServiceTest in an OSGi environment");
            // Note: This timeout of 2 minutes is just for debugging purposes and should not be used in production!
            securityService = FullyInitializedReplicableTracker.createAndOpen(Activator.getContext(), SecurityService.class).getInitializedService(180 * 1000);
        }
        return securityService;
    }
}
