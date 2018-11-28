package com.sap.sse.security.userstore.mongodb.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.ServerInfo;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.PreferenceConverterRegistrationManager;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    private static BundleContext context;
    private ServiceRegistration<?> accessControlStoreRegistration;
    private ServiceRegistration<?> userStoreRegistration;
    private PreferenceConverterRegistrationManager preferenceConverterRegistrationManager;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        logger.info("Creating user store");
        final String defaultTenantName = System.getProperty(UserStore.DEFAULT_TENANT_NAME_PROPERTY_NAME,
                ServerInfo.getName() + "-server");
        final UserStoreImpl userStore = new UserStoreImpl(defaultTenantName);
        AccessControlStoreImpl accessControlStore = new AccessControlStoreImpl(userStore);
        accessControlStoreRegistration = context.registerService(AccessControlStore.class.getName(),
                accessControlStore, null);
        userStoreRegistration = context.registerService(UserStore.class.getName(),
                userStore, null);
        preferenceConverterRegistrationManager = new PreferenceConverterRegistrationManager(bundleContext, userStore);
        logger.info("User store registered.");
        for (CollectionNames name : CollectionNames.values()) {
            MongoDBService.INSTANCE.registerExclusively(CollectionNames.class, name.name());
        }

        new Thread(getClass().getName() + " migration handler") {
            public void run() {
                try {
                    final ServiceTracker<SecurityService, SecurityService> securityServiceServiceTracker = new ServiceTracker<>(
                            context, SecurityService.class, null);
                    securityServiceServiceTracker.open();
                    final SecurityService securityService = securityServiceServiceTracker.waitForService(0);
                    for (User user : userStore.getUsers()) {
                        securityService.migrateOwnership(user, SecuredSecurityTypes.getAllInstances());
                    }
                    for (UserGroup group : userStore.getUserGroups()) {
                        securityService.migrateOwnership(group, SecuredSecurityTypes.getAllInstances());
                    }
                    securityService.assumeOwnershipMigrated(SecuredSecurityTypes.ROLE_DEFINITION.getName(),
                            SecuredSecurityTypes.getAllInstances());
                    securityService.assumeOwnershipMigrated(SecuredSecurityTypes.SERVER.getName(),
                            SecuredSecurityTypes.getAllInstances());
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Error in migration", e);
                }
            }
        }.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        preferenceConverterRegistrationManager.stop();
        accessControlStoreRegistration.unregister();
        userStoreRegistration.unregister();
        Activator.context = null;
    }

}
