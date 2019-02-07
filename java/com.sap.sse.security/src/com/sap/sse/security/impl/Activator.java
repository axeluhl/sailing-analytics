package com.sap.sse.security.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.cache.CacheManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.ServerInfo;
import com.sap.sse.mail.MailService;
import com.sap.sse.replication.Replicable;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.RolePrototypeProvider;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.shared.HasPermissionsProvider;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.ServiceTrackerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static BundleContext context;
    private static CompletableFuture<SecurityService> securityService = new CompletableFuture<>();
    private ServiceRegistration<?> registration;
    
    /**
     * In a non-OSGi test environment, having Shiro instantiate this class with a
     * default constructor makes it difficult to get access to the user store
     * implementation which may live in a bundle that this bundle has no direct
     * access to. Therefore, test cases must set the UserStore implementation
     * by invoking {@link #setTestUserStore} before the default constructor is
     * invoked.
     */
    private static UserStore testUserStore;
    private static AccessControlStore testAccessControlStore;

    private ServiceTracker<RolePrototypeProvider, RolePrototypeProvider> rolePrototypeProviderTracker;

    private ServiceTracker<UserStore, UserStore> userStoreTracker;

    private ServiceTracker<AccessControlStore, AccessControlStore> accessControlStoreTracker;
    
    public static void setTestStores(UserStore theTestUserStore, AccessControlStore theTestAccessControlStore) {
        testUserStore = theTestUserStore;
        testAccessControlStore = theTestAccessControlStore;
        UsernamePasswordRealm.setTestStores(theTestUserStore, theTestAccessControlStore);
    }
    
    public static void setSecurityService(SecurityService securityService) {
        Activator.securityService.complete(securityService);
    }
    
    public static BundleContext getContext() {
        return context;
    }
    
    public static SecurityService getSecurityService() {
        try {
            return securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Failure to get SecurityService", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * If no {@link #testUserStore} is available, start looking for a {@link UserStore} service in a background thread.
     * As soon as a service reference for a {@link UserStore} implementation becomes available in the service registry.
     * If a {@link UserStore} has been found, one way or another, the {@link SecurityService} is created and
     * registered as an OSGi service.
     */
    public void start(BundleContext bundleContext) throws Exception {
        context = bundleContext;
        if (testUserStore != null && testAccessControlStore != null) {
            createAndRegisterSecurityService(bundleContext, testUserStore, testAccessControlStore);
        } else {
            waitForUserStoreService(bundleContext);
        }
        context.registerService(ClearStateTestSupport.class, new ClearStateTestSupport() {
            
            @Override
            public void clearState() throws Exception {
                Activator.this.clearState();
            }
        }, null);
    }

    /**
     * This method will clean the userStore and AccessControllStore, and reset the securityService. It will then
     * reinitialize them in the same fashion an empty server will do.
     */
    protected void clearState() throws InterruptedException, UserGroupManagementException, UserManagementException {
        CacheManager cm = getSecurityService().getCacheManager();
        if (cm instanceof ReplicatingCacheManager) {
            ((ReplicatingCacheManager) cm).clear();
        }

        UserStore userStore = userStoreTracker.waitForService(0);
        AccessControlStore accessControlStore = accessControlStoreTracker.waitForService(0);
        userStore.clear();
        accessControlStore.clear();
        createRoleDefinitionsFromPrototypes(context, userStore);
        userStore.ensureDefaultRolesExist();
        userStore.ensureDefaultTenantExists();
        getSecurityService().initialize();
    }

    private void createAndRegisterSecurityService(BundleContext bundleContext, UserStore userStore, AccessControlStore accessControlStore) {
        final ServiceTracker<HasPermissionsProvider, HasPermissionsProvider> hasPermissionsProviderTracker = new ServiceTracker<>(
                bundleContext, HasPermissionsProvider.class, /* customizer */ null);
        hasPermissionsProviderTracker.open();
        SecurityServiceImpl initialSecurityService = new SecurityServiceImpl(
                ServiceTrackerFactory.createAndOpen(context, MailService.class), userStore, accessControlStore,
                new OSGIHasPermissionsProvider(hasPermissionsProviderTracker));
        initialSecurityService.initialize();
        securityService.complete(initialSecurityService);
        registration = context.registerService(SecurityService.class.getName(), initialSecurityService, null);
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name,
                initialSecurityService.getId().toString());
        context.registerService(Replicable.class.getName(), initialSecurityService, replicableServiceProperties);
        context.registerService(ClearStateTestSupport.class.getName(), initialSecurityService, null);
        Logger.getLogger(Activator.class.getName()).info("Security Service registered.");
    }
    
    private void createRoleDefinitionsFromPrototypes(BundleContext bundleContext, UserStore userStore) {
        rolePrototypeProviderTracker = new ServiceTracker<>(
                bundleContext, RolePrototypeProvider.class, /* customizer */ new ServiceTrackerCustomizer<RolePrototypeProvider, RolePrototypeProvider>() {
                    @Override
                    public RolePrototypeProvider addingService(ServiceReference<RolePrototypeProvider> reference) {
                        final RolePrototypeProvider service = context.getService(reference);
                        final RolePrototype rolePrototype = service.getRolePrototype();
                        final RoleDefinition potentiallyExistingRoleDefinition = userStore.getRoleDefinition(rolePrototype.getId());
                        if (potentiallyExistingRoleDefinition == null) {
                            userStore.createRoleDefinition(rolePrototype.getId(), rolePrototype.getName(), rolePrototype.getPermissions());
                        }
                        return service;
                    }

                    @Override
                    public void modifiedService(ServiceReference<RolePrototypeProvider> reference,
                            RolePrototypeProvider service) {
                    }

                    @Override
                    public void removedService(ServiceReference<RolePrototypeProvider> reference,
                            RolePrototypeProvider service) {
                    }
                });
        rolePrototypeProviderTracker.open();
    }

    private void waitForUserStoreService(BundleContext bundleContext) {
        context = bundleContext;
        userStoreTracker = new ServiceTracker<>(bundleContext, UserStore.class, /* customizer */ null);
        accessControlStoreTracker = new ServiceTracker<>(bundleContext, AccessControlStore.class,
                /* customizer */ null);
        userStoreTracker.open();
        accessControlStoreTracker.open();
        new Thread("ServiceTracker waiting for UserStore service") {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // ensure that classpath:... Shiro ini files are resolved properly
                    logger.info("Waiting for UserStore service...");
                    final UserStore userStore = userStoreTracker.waitForService(0);
                    final AccessControlStore accessControlStore = accessControlStoreTracker.waitForService(0);
                    logger.info("Obtained UserStore service "+userStore);
                    createRoleDefinitionsFromPrototypes(bundleContext, userStore);
                    // must be called after the definition of the prototypes and after loading actual roles, but before
                    // loading users
                    userStore.ensureDefaultRolesExist();
                    // actually load the users and migrate them if required
                    userStore.loadAndMigrateUsers();
                    // loading ACLs and Ownerships requires users and UserGroups to be correctly loaded
                    accessControlStore.loadACLsAndOwnerships();
                    // create security service, it will also create a default admin user if no users exist
                    createAndRegisterSecurityService(bundleContext, userStore, accessControlStore);
                    // check if we already have an ownership for the server, create if it is missing
                    QualifiedObjectIdentifier expectedServerOwner = SecuredSecurityTypes.SERVER
                            .getQualifiedObjectIdentifier(
                                    new TypeRelativeObjectIdentifier(ServerInfo.getName()));
                    getSecurityService().setOwnershipIfNotSet(expectedServerOwner, null, userStore.getDefaultTenant());
                } catch (InterruptedException | UserGroupManagementException | UserManagementException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
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
        if (rolePrototypeProviderTracker != null) {
            rolePrototypeProviderTracker.close();
            rolePrototypeProviderTracker = null;
        }
        if (registration != null) {
            registration.unregister();
        }
        Activator.context = null;
    }
}
