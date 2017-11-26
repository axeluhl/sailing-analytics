package com.sap.sse.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RolePermissionModel;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * This class implements a realm that combines Access Control Lists, Role Based Permission Modeling and
 * Ownership for users as well as tenants.
 * 
 * This class overrides all the methods of {@link AuthorizingRealm} that call doGetAuthorizationInfo. Since
 * this realm is highly dynamic, doGetAuthorizationInfo cannot be easily implemented and is thus never called.
 * 
 * @author Jonas Dann
 */
public abstract class AbstractCompositeAuthrizingRealm extends AuthorizingRealm implements RolePermissionModel {
    private static final Logger logger = Logger.getLogger(AbstractCompositeAuthrizingRealm.class.getName());
    private final Future<UserStore> userStore;
    private final Future<AccessControlStore> aclStore;

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
    public boolean isPermittedAll(PrincipalCollection principals, Collection<Permission> permissions) {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
                if (!isPermitted(principals, permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void checkPermission(PrincipalCollection principals, Permission permission) throws AuthorizationException {
        if (!isPermitted(principals, permission)) {
            String msg = "User is not permitted [" + permission + "]";
            throw new UnauthorizedException(msg);
        }
    }

    @Override
    public void checkPermissions(PrincipalCollection principals, Collection<Permission> permissions) throws AuthorizationException {
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission permission : permissions) {
                checkPermission(principals, permission);
            }
        }
    }
    
    @Override
    public boolean hasRole(PrincipalCollection principals, String roleIdentifier) {
        String user = (String) principals.getPrimaryPrincipal();
        try {
            for (UUID role : getUserStore().getRolesFromUser(user)) {
                if (role.equals(UUID.fromString(roleIdentifier))) {
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
    public boolean[] hasRoles(PrincipalCollection principals, List<String> roleIdentifiers) {
        boolean[] result = new boolean[roleIdentifiers.size()];
        for (int i = 0; i < roleIdentifiers.size(); i++) {
            result[i] = hasRole(principals, roleIdentifiers.get(i));
        }
        return result;
    }
    
    @Override
    public boolean hasAllRoles(PrincipalCollection principals, Collection<String> roleIdentifiers) {
        if (roleIdentifiers != null && !roleIdentifiers.isEmpty()) {
            for (String role : roleIdentifiers) {
                if (!hasRole(principals, role)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public void checkRole(PrincipalCollection principals, String role) throws AuthorizationException {
        if (!hasRole(principals, role)) {
            String msg = "User does not have role [" + role + "]";
            throw new UnauthorizedException(msg);
        }
    }
    
    @Override
    public void checkRoles(PrincipalCollection principals, Collection<String> roles) throws AuthorizationException {
        if (roles != null && !roles.isEmpty()) {
            for (String role : roles) {
                checkRole(principals, role);
            }
        }
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // As all the public methods of AuthorizingRealm are overridden to not use this, this should never be called.
        throw new UnsupportedOperationException("Call to doGetAuthorizationInfo(PrincipalCollection principals. This should never happen!)");
    }
    
    private Role getRole(UUID id) {
        return getAccessControlListStore().getRole(id);
    }
    
    @Override
    public String getName(UUID id) {
        Role role = getRole(id);
        if (role != null) {
            return role.getName();
        } else {
            return "";
        }
    }
    
    @Override
    public Iterable<WildcardPermission> getPermissions(UUID id) {
        Role role = getRole(id);
        if (role != null) {
            return role.getPermissions();
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public Iterable<Role> getRoles() {
        return getAccessControlListStore().getRoles();
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(UUID id, WildcardPermission permission) {
        return implies(id, permission, null);
    }
    
    @Override
    public boolean implies(UUID id, WildcardPermission permission, Owner ownership) {
        Role role = getRole(id);
        if (role != null) {
            return implies(id, role.getName(), permission, ownership);
        } else {
            return false;
        }
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(UUID id, String name, WildcardPermission permission, Owner ownership) {
        String[] parts = name.split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().equals(parts[1]))) {
            for (WildcardPermission rolePermission : getPermissions(id)) {
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
