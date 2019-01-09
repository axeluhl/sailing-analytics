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
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.PermissionAndRoleAssociation;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
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

        new Thread(getClass().getName() + "userstore after SecurityService async startup") {
            public void run() {
                try {
                    final ServiceTracker<SecurityService, SecurityService> securityServiceServiceTracker = new ServiceTracker<>(
                            context, SecurityService.class, null);
                    securityServiceServiceTracker.open();
                    final SecurityService securityService = securityServiceServiceTracker.waitForService(0);
                    userStore.loadAndMigrateUsers(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory());

                    for (User user : userStore.getUsers()) {
                        securityService.migrateOwnership(user);
                    }
                    for (UserGroup group : userStore.getUserGroups()) {
                        securityService.migrateOwnership(group);
                    }
                    for (RoleDefinition role : userStore.getRoleDefinitions()) {
                        securityService.migrateOwnership(role);
                    }
                    for (User user : securityService.getUserList()) {
                        for (Role role : user.getRoles()) {
                            TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation.get(role, user);
                            QualifiedObjectIdentifier associationQualifiedIdentifier = SecuredSecurityTypes.ROLE_ASSOCIATION
                                    .getQualifiedObjectIdentifier(associationTypeIdentifier);
                            securityService.migrateOwnership(associationQualifiedIdentifier, associationTypeIdentifier.toString());
                        }
                        for (WildcardPermission permission : user.getPermissions()) {
                            TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation
                                    .get(permission, user);
                            QualifiedObjectIdentifier associationQualifiedIdentifier = SecuredSecurityTypes.PERMISSION_ASSOCIATION
                                    .getQualifiedObjectIdentifier(associationTypeIdentifier);
                            securityService.migrateOwnership(associationQualifiedIdentifier,
                                    associationTypeIdentifier.toString());
                        }
                    }

                    securityService.assumeOwnershipMigrated(SecuredSecurityTypes.SERVER.getName());
                    securityService.checkMigration(SecuredSecurityTypes.getAllInstances());
                } catch (InterruptedException | UserGroupManagementException | UserManagementException e) {
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
