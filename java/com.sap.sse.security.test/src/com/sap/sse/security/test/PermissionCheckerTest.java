package com.sap.sse.security.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.AbstractCompositeAuthorizingRealm;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AdminRole;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.Ownership;
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
    private final WildcardPermission eventReadPermission = SecuredDomainType.EVENT.getPermissionForObjects(DefaultActions.READ, eventId.toString());
    private final UUID userTenantId = UUID.randomUUID();
    private UserGroup adminTenant;
    private SecurityUser adminUser;
    private UserGroup userTenant;
    private User user;
    private User allUser;
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
        allUser = userStore.getUserByName(SecurityService.ALL_USERNAME);
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
        permissionSet.add(eventReadPermission);
        globalRoleDefinition = new RoleDefinitionImpl(globalRoleId, "event", permissionSet);
        principalCollection = mock(PrincipalCollection.class);
        when(principalCollection.getPrimaryPrincipal()).thenReturn(user.getName());
    }
    
    @Test
    public void testOwnership() throws UserManagementException {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                null, acl));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        // being the owning user does not imply any permissions per se
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                ownership, acl));
        userStore.addRoleForUser(user.getName(), new RoleImpl(AdminRole.getInstance(), /* qualified for userTenant */ null, /* qualified for user */ user));
        // having the admin role qualified for objects owned by user should help
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                ownership, acl));
    }
    
    /**
     * {@link SecuredDomainType} objects may specify an object ID in their third part. When checking a permission,
     * ownership information needs to be obtained for the object(s) in question because it affects the
     * permission check. For example, a user may have a role that applies its permissions only to objects
     * that the user owns or where the user belongs to the group that owns the object. Therefore, it must be
     * possible to look up the ownership information based on the object ID provided in the third part of the
     * {@link SecuredDomainType} object. This test sets up objects of different kinds, specifies ownerships for them,
     * sets up users and roles with qualifications and then validates that the correct permissions emerge based
     * on a successful ownership lookup with the object ID provided by the permission.
     */
    @Test
    public void testPermissionsImpliedByOwnershipConstrainedRole() throws UserManagementException {
        final String leaderboardName = "My:Leaderboard, the only one ";
        final String regattaName = " My:Regatta, the only one ";
        WildcardPermission leaderboardPermission = SecuredDomainType.LEADERBOARD.getPermissionForObjects(DefaultActions.READ, leaderboardName);
        WildcardPermission regattaPermission = SecuredDomainType.REGATTA.getPermissionForObjects(DefaultActions.READ, regattaName);
        assertFalse(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        // let leaderboard be owned by user
        accessControlStore.setOwnership(SecuredDomainType.LEADERBOARD.getQualifiedObjectIdentifier(leaderboardName), user,
                /* tenantOwner */ null, leaderboardName);
        // let regatta be owned by admin
        accessControlStore.setOwnership(SecuredDomainType.REGATTA.getQualifiedObjectIdentifier(regattaName), adminUser,
                /* tenantOwner */ null, regattaName);
        // grant user the admin role, but only for objects owned by the user (leaderboard, but not regatta)
        userStore.addRoleForUser(user.getName(), new RoleImpl(AdminRole.getInstance(), /* qualifiedForTenant */ null, /* qualifiedForUser */ user));
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        accessControlStore.setOwnership(SecuredDomainType.REGATTA.getQualifiedObjectIdentifier(regattaName), /* userOwner */ null,
                /* groupOwner */ userTenant, leaderboardName);
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        // only adding the group owner doesn't grant permission yet:
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        // but now we assign the admin role to the user, qualified for objects owned by the group owner:
        userStore.addRoleForUser(user.getName(), new RoleImpl(AdminRole.getInstance(), /* qualifiedForTenant */ userTenant, /* qualifiedForUser */ null));
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        // now the user should be granted permission because admin gets *, and the user gets admin on all objects owned by userTenant
        assertTrue(realm.isPermitted(principalCollection, regattaPermission.toString()));
    }
    
    @Test
    public void testAccessControlList() {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, null));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        acl.addPermission(userTenant, DefaultActions.READ.name());
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        // ensure that anonymous users don't have access because they don't belong to any group
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, /* user */ null, /* groups */ new HashSet<>(),
                allUser, allUser.getUserGroups(), adminOwnership, acl));
        user.addPermission(eventReadPermission);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        final Set<String> permissionSet = new HashSet<>();
        permissionSet.add("!" + DefaultActions.READ.name());
        acl.setPermissions(userTenant, permissionSet);
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        // User ownership shall NOT imply permissions; the revoking ACL still takes precedence
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                ownership, acl));
        // now add "public read" permission to ACL:
        acl.addPermission(null, DefaultActions.READ.name());
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, /* user */ null, /* groups */ new HashSet<>(),
                allUser, allUser.getUserGroups(), adminOwnership, acl));
        // now deny "public read" permission in ACL which is expected to supersede the granting from above:
        acl.denyPermission(null, DefaultActions.READ.name());
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, /* user */ null, /* groups */ new HashSet<>(),
                allUser, allUser.getUserGroups(), adminOwnership, acl));
    }
    
    @Test
    public void testDirectPermission() {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        user.addPermission(eventReadPermission);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
    }
    
    @Test
    public void testRole() {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        final RoleImpl globalRole = new RoleImpl(globalRoleDefinition);
        user.addRole(globalRole);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        user.removeRole(globalRole);
        user.addRole(new RoleImpl(globalRoleDefinition, this.userTenant, /* user qualifier */ null));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                adminOwnership, acl));
        Ownership testOwnership = new OwnershipImpl(adminUser, userTenant);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                testOwnership, acl));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, allUser, allUser.getUserGroups(),
                null, acl));
    }
}