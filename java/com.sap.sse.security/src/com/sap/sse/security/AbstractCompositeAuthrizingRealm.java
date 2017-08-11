package com.sap.sse.security;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.UserManagementException;

public abstract class AbstractCompositeAuthrizingRealm extends AuthorizingRealm {
    private static final Logger logger = Logger.getLogger(AbstractCompositeAuthrizingRealm.class.getName());
    private final Future<UserStore> userStore;
    private final Future<AccessControlStore> aclStore;
    private PermissionsForRoleProvider permissionsForRoleProvider;

    /**
     * In a non-OSGi test environment, having Shiro instantiate this class with a default constructor makes it difficult
     * to get access to the user store implementation which may live in a bundle that this bundle has no direct access
     * to. Therefore, test cases must set the UserStore implementation by invoking {@link #setTestUserStore} before the
     * default constructor is invoked.
     */
    private static UserStore testUserStore;
    private static AccessControlStore testAclStore;

    public static void setTestUserStore(UserStore theTestUserStore) {
        testUserStore = theTestUserStore;
    }
    
    public static void setTestAclStore(AccessControlStore theTestAclStore) {
        testAclStore = theTestAclStore;
    }

    public AbstractCompositeAuthrizingRealm() {
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
    
    private Future<AccessControlStore> createAclStoreFuture(BundleContext bundleContext) {
        final ServiceTracker<AccessControlStore, AccessControlStore> tracker = new ServiceTracker<>(bundleContext, AccessControlStore.class, /* customizer */ null);
        tracker.open();
        final FutureTask<AccessControlStore> result = new FutureTask<>(new Callable<AccessControlStore>() {
            @Override
            public AccessControlStore call() throws InterruptedException {
                try {
                    logger.info("Waiting for AccessControlListStore service...");
                    AccessControlStore aclStore = tracker.waitForService(0);
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
    
    protected AccessControlStore getAccessControlListStore() {
        AccessControlStore result;
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
    public boolean isPermitted(PrincipalCollection principals, Permission perm) {
        String[] parts = perm.toString().split(":");
        if (parts.length < 3) {
            throw new WrongPermissionFormatException(perm);
        }
        String user = (String) principals.getPrimaryPrincipal();
        Owner ownership = getAccessControlListStore().getOwnership(parts[3]);
        if (user.equals(ownership.getOwner())) { // TODO check for tenant ownership
            return true;
        }
        AccessControlList acl = getAccessControlListStore().getAccessControlListByName(parts[3]);
        if (acl.hasPermission(user, parts[2])) {
            return true;
        } else if (acl.hasPermission(user, "!" + parts[2])) {
            return false;
        }
        try {
            for (String directPermission : getUserStore().getPermissionsFromUser(user)) {
                Permission directPerm = getPermissionResolver().resolvePermission(directPermission);
                if (directPerm.implies(perm)) {
                    return true;
                }
            }
        } catch (UserManagementException e) {
            logger.log(Level.SEVERE, "User " + user + " does not exist.", e);
            return false;
        }
        try {
            for (String role : getUserStore().getRolesFromUser(user)) {
                for (String rolePermission : permissionsForRoleProvider.getPermissions(role)) {
                    Permission rolePerm = getPermissionResolver().resolvePermission(rolePermission);
                    if (rolePerm.implies(perm)) {
                        return true;
                    }
                }
            }
        } catch (UserManagementException e) {
            logger.log(Level.SEVERE, "User " + user + " does not exist.", e);
            return false;
        }
        return false;
    }

    @Override
    public boolean[] isPermitted(PrincipalCollection principals, List<Permission> permissions) {
        boolean[] result = new boolean[permissions.size()];
        for (int i = 0; i < permissions.size(); i++) {
            result[i] = isPermitted(principals, permissions.get(i));
        }
        return result;
    }

    @Override
    public boolean isPermittedAll(PrincipalCollection principal, Collection<Permission> permissions) {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
                if (!isPermitted(principal, permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void checkPermission(PrincipalCollection principal, Permission permission) throws AuthorizationException {
        // TODO
    }

    @Override
    public void checkPermissions(PrincipalCollection principal, Collection<Permission> permissions) throws AuthorizationException {
        // TODO
    }
    
    @Override
    public boolean hasRole(PrincipalCollection principal, String roleIdentifier) {
        String user = (String) principal.getPrimaryPrincipal();
        try {
            for (String role : getUserStore().getRolesFromUser(user)) {
                if (role.equals(roleIdentifier)) {
                    return true;
                }
            }
        } catch (UserManagementException e) {
            logger.log(Level.SEVERE, "User " + user + " does not exist.", e);
            return false;
        }
        return false;
    }

    @Override
    public boolean[] hasRoles(PrincipalCollection principal, List<String> roleIdentifiers) {
        boolean[] result = new boolean[roleIdentifiers.size()];
        for (int i = 0; i < roleIdentifiers.size(); i++) {
            result[i] = hasRole(principal, roleIdentifiers.get(i));
        }
        return result;
    }
    
    @Override
    public boolean hasAllRoles(PrincipalCollection principal, Collection<String> roleIdentifiers) {
        if (roleIdentifiers != null && !roleIdentifiers.isEmpty()) {
            for (String role : roleIdentifiers) {
                if (!hasRole(principal, role)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public void checkRole(PrincipalCollection principal, String role) throws AuthorizationException {
        // TODO
    }
    
    @Override
    public void checkRoles(PrincipalCollection principal, Collection<String> roles) throws AuthorizationException {
        // TODO
    }

    @Override
    public void checkRoles(PrincipalCollection principal, String... roles) throws AuthorizationException {
        // TODO
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null; // As all the public methods of AuthorizingRealm are overridden to not use this, this should never be called.
    }
}
