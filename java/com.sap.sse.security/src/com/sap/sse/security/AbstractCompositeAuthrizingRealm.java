package com.sap.sse.security;

import java.util.ArrayList;
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
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.RolePermissionModel;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;

public abstract class AbstractCompositeAuthrizingRealm extends AuthorizingRealm implements RolePermissionModel {
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
        String[] parts = perm.toString().replaceAll("\\[|\\]", "").split(":");
        String user = (String) principals.getPrimaryPrincipal();
        
        try {
            ArrayList<WildcardPermission> directPermissions = new ArrayList<>();
            for (String directPermission : getUserStore().getPermissionsFromUser(user)) {
                directPermissions.add(new WildcardPermission(directPermission));
            }
            
            return PermissionChecker.isPermitted(new WildcardPermission(perm.toString().replaceAll("\\[|\\]", "")), 
                    user, getUserStore().getUserGroups(), directPermissions, 
                    getUserStore().getRolesFromUser(user), this, 
                    getAccessControlListStore().getOwnership(parts[2]), 
                    getAccessControlListStore().getAccessControlList(parts[2]));
        } catch (UserManagementException e) {
            logger.log(Level.SEVERE, "User " + user + " does not exist.", e);
            return false;
        }
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
    
    @Override
    public Iterable<String> getPermissions(String role) {
        return permissionsForRoleProvider.getPermissions(role, null);
    }
    
    @Override
    public boolean implies(String role, WildcardPermission permission) { // TODO as default implementation in interface
        return implies(role, permission, null);
    }
    
    @Override
    public boolean implies(String role, WildcardPermission permission, Owner ownership) { // TODO as default implementation in interface
        String[] parts = role.split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().equals(parts[1]))) {
            for (String rolePermissionString : getPermissions(role)) {
                WildcardPermission rolePermission = new WildcardPermission(rolePermissionString, true);
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
