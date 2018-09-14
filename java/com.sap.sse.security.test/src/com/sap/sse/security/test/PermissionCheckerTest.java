package com.sap.sse.security.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.security.Permission;
import com.sap.sse.security.AbstractCompositeAuthorizingRealm;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Permission.DefaultModes;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class PermissionCheckerTest {
    private final UUID eventId = UUID.randomUUID();
    private final WildcardPermission permission = Permission.EVENT.getPermissionForObjects(DefaultModes.READ, eventId.toString());
    private final UUID userTenantId = UUID.randomUUID();
    private UserGroup adminTenant;
    private SecurityUser adminUser;
    private UserGroup userTenant;
    private User user;
    private ArrayList<UserGroup> tenants;
    private Ownership ownership;
    private Ownership adminOwnership;
    private AccessControlList acl;
    private final UUID globalRoleId = UUID.randomUUID();
    private RoleDefinition globalRoleDefinition;
    private AbstractCompositeAuthorizingRealm realm;
    private UserStore userStore;
    private AccessControlStore accessControlStore;
    private PrincipalCollection principalCollection;
    
    @Before
    public void setUp() throws UserGroupManagementException, UserManagementException {
        final String adminTenantName = "admin-tenant";
        userStore = new UserStoreImpl(adminTenantName);
        accessControlStore = new AccessControlStoreImpl(userStore);
        AbstractCompositeAuthorizingRealm.setTestStores(userStore, accessControlStore);
        realm = new UsernamePasswordRealm();
        adminUser = userStore.getUserByName("admin");
        adminTenant = userStore.getUserGroupByName(adminTenantName);
        if (userStore.getUserByName("jonas") != null) {
            userStore.deleteUser("jonas");
        }
        user = userStore.createUser("jonas", "jonas@dann.io", userTenant);
        userTenant = userStore.createUserGroup(userTenantId, "jonas-tenant");
        userTenant.add(user);
        ownership = new OwnershipImpl(user, userTenant);
        adminTenant.add(adminUser);
        adminOwnership = new OwnershipImpl(adminUser, adminTenant);
        tenants = new ArrayList<>();
        tenants.add(userTenant);
        tenants.add(adminTenant);
        acl = new AccessControlListImpl();
        Set<WildcardPermission> permissionSet = new HashSet<>();
        permissionSet.add(permission);
        globalRoleDefinition = new RoleDefinitionImpl(globalRoleId, "event", permissionSet);
        principalCollection = mock(PrincipalCollection.class);
        when(principalCollection.getPrimaryPrincipal()).thenReturn(user.getName());
    }
    
    @Test
    public void testOwnership() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, null, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, ownership, acl));
    }
    
    /**
     * {@link Permission} objects may specify an object ID in their third part. When checking a permission,
     * ownership information needs to be obtained for the object(s) in question because it affects the
     * permission check. For example, a user may have a role that applies its permissions only to objects
     * that the user owns or where the user belongs to the group that owns the object. Therefore, it must be
     * possible to look up the ownership information based on the object ID provided in the third part of the
     * {@link Permission} object. This test sets up objects of different kinds, specifies ownerships for them,
     * sets up users and roles with qualifications and then validates that the correct permissions emerge based
     * on a successful ownership lookup with the object ID provided by the permission.
     */
    @Test
    public void testPermissionsImpliedByOwnershipConstrainedRole() {
        final String leaderboardName = "My:Leaderboard, the only one ";
        final String regattaName = " My:Regatta, the only one ";
        WildcardPermission leaderboardPermission = Permission.LEADERBOARD.getPermissionForObjects(DefaultModes.READ, leaderboardName);
        WildcardPermission regattaPermission = Permission.REGATTA.getPermissionForObjects(DefaultModes.READ, regattaName);
        assertFalse(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        accessControlStore.createOwnership(Permission.LEADERBOARD.getQualifiedObjectIdentifier(leaderboardName), user,
                /* tenantOwner */ null, leaderboardName);
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        accessControlStore.createOwnership(Permission.REGATTA.getQualifiedObjectIdentifier(regattaName), user,
                /* tenantOwner */ null, leaderboardName);
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertTrue(realm.isPermitted(principalCollection, regattaPermission.toString()));
    }
    
    @Test
    public void testAccessControlList() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, null));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(DefaultModes.READ.name());
        permissionMap.put(userTenant, permissionSet);
        acl = new AccessControlListImpl(permissionMap);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        user.addPermission(permission);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        permissionMap = new HashMap<>();
        permissionSet = new HashSet<>();
        permissionSet.add("!" + DefaultModes.READ.name());
        permissionMap.put(userTenant, permissionSet);
        acl = new AccessControlListImpl(permissionMap);
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, ownership, acl));
    }
    
    @Test
    public void testDirectPermission() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        user.addPermission(permission);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
    }
    
    @Test
    public void testRole() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        final RoleImpl globalRole = new RoleImpl(globalRoleDefinition);
        user.addRole(globalRole);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        user.removeRole(globalRole);
        user.addRole(new RoleImpl(globalRoleDefinition, this.userTenant, /* user qualifier */ null));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        Ownership testOwnership = new OwnershipImpl(adminUser, userTenant);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, testOwnership, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, null, acl));
    }
}