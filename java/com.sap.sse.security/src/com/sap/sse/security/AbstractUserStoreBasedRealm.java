package com.sap.sse.security;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.common.Util;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.UserManagementException;

public abstract class AbstractUserStoreBasedRealm extends AuthorizingRealm {
    private static final Logger logger = Logger.getLogger(AbstractUserStoreBasedRealm.class.getName());
    private final Future<UserStore> userStore;
    private final Future<AccessControlListStore> aclStore;
    private PermissionsForRoleProvider permissionsForRoleProvider;

    /**
     * In a non-OSGi test environment, having Shiro instantiate this class with a default constructor makes it difficult
     * to get access to the user store implementation which may live in a bundle that this bundle has no direct access
     * to. Therefore, test cases must set the UserStore implementation by invoking {@link #setTestUserStore} before the
     * default constructor is invoked.
     */
    private static UserStore testUserStore;
    private static AccessControlListStore testAclStore;

    public static void setTestUserStore(UserStore theTestUserStore) {
        testUserStore = theTestUserStore;
    }
    
    public static void setTestAclStore(AccessControlListStore theTestAclStore) {
        testAclStore = theTestAclStore;
    }

    public AbstractUserStoreBasedRealm() {
        super();
        setCachingEnabled(false); // always grab fresh authorization info from the user store
        BundleContext context = Activator.getContext();
        if (context != null) {
            userStore = createUserStoreFuture(context);
            aclStore = createAclStoreFuture(context);
        } else {
            userStore = null;
            aclStore = null;
        }
    }
    
    public void setPermissionsForRoleProvider(PermissionsForRoleProvider permissionsForRoleProvider) {
        this.permissionsForRoleProvider = permissionsForRoleProvider;
    }

    private Future<UserStore> createUserStoreFuture(BundleContext bundleContext) {
        final ServiceTracker<UserStore, UserStore> tracker = new ServiceTracker<>(bundleContext, UserStore.class, /* customizer */ null);
        tracker.open();
        final FutureTask<UserStore> result = new FutureTask<>(new Callable<UserStore>() {
            @Override
            public UserStore call() throws InterruptedException {
                try {
                    logger.info("Waiting for UserStore service...");
                    UserStore userStore = tracker.waitForService(0);
                    logger.info("Obtained UserStore service "+userStore);
                    return userStore;
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                    throw e;
                }
            }
        });
        new Thread("ServiceTracker waiting for UserStore service") {
            @Override
            public void run() {
                try {
                    result.run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while waiting for UserStore service", e);
                }
            }
        }.start();
        return result;
    }
    
    private Future<AccessControlListStore> createAclStoreFuture(BundleContext bundleContext) {
        final ServiceTracker<AccessControlListStore, AccessControlListStore> tracker = new ServiceTracker<>(bundleContext, AccessControlListStore.class, /* customizer */ null);
        tracker.open();
        final FutureTask<AccessControlListStore> result = new FutureTask<>(new Callable<AccessControlListStore>() {
            @Override
            public AccessControlListStore call() throws InterruptedException {
                try {
                    logger.info("Waiting for AccessControlListStore service...");
                    AccessControlListStore aclStore = tracker.waitForService(0);
                    logger.info("Obtained AccessControlListStore service "+aclStore);
                    return aclStore;
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for AccessControlListStore service", e);
                    throw e;
                }
            }
        });
        new Thread("ServiceTracker waiting for AccessControlListStore service") {
            @Override
            public void run() {
                try {
                    result.run();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while waiting for AccessControlListStore service", e);
                }
            }
        }.start();
        return result;
    }

    protected UserStore getUserStore() {
        UserStore result;
        if (testUserStore != null) {
            result = testUserStore;
        } else {
            try {
                result = userStore.get();
            } catch (InterruptedException | ExecutionException e) {
                result = null;
                logger.log(Level.SEVERE, "Error retrieving user store", e);
            }
        }
        return result;
    }
    
    protected AccessControlListStore getAccessControlListStore() {
        AccessControlListStore result;
        if (testAclStore != null) {
            result = testAclStore;
        } else {
            try {
                result = aclStore.get();
            } catch (InterruptedException | ExecutionException e) {
                result = null;
                logger.log(Level.SEVERE, "Error retrieving access control list store", e);
            }
        }
        return result;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        final SimpleAuthorizationInfo ai = new SimpleAuthorizationInfo();
        final List<String> roles = new ArrayList<>();
        final List<String> permissions = new ArrayList<>();
        for (Object r : principals) {
            String username = r.toString();
            try {
                Util.addAll(getUserStore().getRolesFromUser(username), roles);
                if (permissionsForRoleProvider != null) {
                    for (String role : roles) {
                        for (String permission : permissionsForRoleProvider.getPermissions(role)) {
                            ai.addStringPermission(permission);
                        }
                    }
                }
                Util.addAll(getUserStore().getPermissionsFromUser(username), permissions);
            } catch (UserManagementException e) {
               throw new AuthenticationException(e.getMessage());
            }
        }
        ai.addRoles(roles);
        ai.addStringPermissions(permissions);
        return ai;
    }
}
