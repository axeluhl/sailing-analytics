package com.sap.sse.security.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.AbstractCompositeAuthorizingRealm;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.shared.AdminRole;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.HasPermissionsImpl;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class PermissionCheckerTest {
    private final UUID eventId = UUID.randomUUID();
    private final WildcardPermission eventReadPermission = SecuredDomainType.EVENT
            .getPermissionForTypeRelativeIdentifier(DefaultActions.READ,
                    new TypeRelativeObjectIdentifier(eventId.toString()));
    private final UUID userTenantId = UUID.randomUUID();
    private UserGroup adminTenant;
    private User adminUser;
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
    private HasPermissions type1 = new HasPermissionsImpl("DEMO", DefaultActions.READ, DefaultActions.UPDATE);
    private HasPermissions type2 = new HasPermissionsImpl("TEST", DefaultActions.READ, DefaultActions.DELETE);
    private Iterable<HasPermissions> allHasPermissions = Arrays.asList(type1, type2);
    
    @Before
    public void setUp() throws UserGroupManagementException, UserManagementException {
        final String adminTenantName = "admin-tenant";
        userStore = new UserStoreImpl(adminTenantName);
        userStore.ensureDefaultRolesExist();
        userStore.ensureDefaultTenantExists();
        accessControlStore = new AccessControlStoreImpl(userStore);
        AbstractCompositeAuthorizingRealm.setTestStores(userStore, accessControlStore);
        realm = new UsernamePasswordRealm();
        adminUser = userStore.getUserByName("admin");
        adminTenant = userStore.getUserGroupByName(adminTenantName);
        if (userStore.getUserByName("jonas") != null) {
            userStore.deleteUser("jonas");
        }
        userTenant = userStore.createUserGroup(userTenantId, "jonas-tenant");
        user = userStore.createUser("jonas", "jonas@dann.io");
        userTenant.add(user);
        ownership = new Ownership(user, userTenant);
        adminTenant.add(adminUser);
        adminOwnership = new Ownership(adminUser, adminTenant);
        tenants = new ArrayList<>();
        tenants.add(userTenant);
        tenants.add(adminTenant);
        acl = new AccessControlList();
        Set<WildcardPermission> permissionSet = new HashSet<>();
        permissionSet.add(eventReadPermission);
        globalRoleDefinition = new RoleDefinitionImpl(globalRoleId, "event", permissionSet);
        principalCollection = mock(PrincipalCollection.class);
        when(principalCollection.getPrimaryPrincipal()).thenReturn(user.getName());
    }
    
    @Test
    public void testOwnership() throws UserManagementException {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                null, acl));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        // being the owning user does not imply any permissions per se
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                ownership, acl));
        userStore.addRoleForUser(user.getName(),
                new Role(AdminRole.getInstance(), /* qualified for userTenant */ null, /* qualified for user */ user));
        // having the admin role qualified for objects owned by user should help
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
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
        TypeRelativeObjectIdentifier leaderboardIdentifier = new TypeRelativeObjectIdentifier(leaderboardName);
        final String regattaName = " My:Regatta, the only one ";
        TypeRelativeObjectIdentifier regattaIdentifier = new TypeRelativeObjectIdentifier(regattaName);
        WildcardPermission leaderboardPermission = SecuredDomainType.LEADERBOARD
                .getPermissionForTypeRelativeIdentifier(DefaultActions.READ, leaderboardIdentifier);
        WildcardPermission regattaPermission = SecuredDomainType.REGATTA.getPermissionForTypeRelativeIdentifier(
                DefaultActions.READ, regattaIdentifier);
        assertFalse(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        // let leaderboard be owned by user
        accessControlStore.setOwnership(SecuredDomainType.LEADERBOARD.getQualifiedObjectIdentifier(leaderboardIdentifier), user,
                /* tenantOwner */ null, leaderboardName);
        // let regatta be owned by admin
        accessControlStore.setOwnership(SecuredDomainType.REGATTA.getQualifiedObjectIdentifier(regattaIdentifier), adminUser,
                /* tenantOwner */ null, regattaName);
        // grant user the admin role, but only for objects owned by the user (leaderboard, but not regatta)
        userStore.addRoleForUser(user.getName(),
                new Role(AdminRole.getInstance(), /* qualifiedForTenant */ null, /* qualifiedForUser */ user));
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        accessControlStore.setOwnership(SecuredDomainType.REGATTA.getQualifiedObjectIdentifier(regattaIdentifier), /* userOwner */ null,
                /* groupOwner */ userTenant, leaderboardName);
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        // only adding the group owner doesn't grant permission yet:
        assertFalse(realm.isPermitted(principalCollection, regattaPermission.toString()));
        // but now we assign the admin role to the user, qualified for objects owned by the group owner:
        userStore.addRoleForUser(user.getName(),
                new Role(AdminRole.getInstance(), /* qualifiedForTenant */ userTenant, /* qualifiedForUser */ null));
        assertTrue(realm.isPermitted(principalCollection, leaderboardPermission.toString()));
        // now the user should be granted permission because admin gets *, and the user gets admin on all objects owned by userTenant
        assertTrue(realm.isPermitted(principalCollection, regattaPermission.toString()));
    }
    
    @Test
    public void testAccessControlList() {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, null));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        acl.addPermission(userTenant, DefaultActions.READ.name());
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        // ensure that anonymous users don't have access because they don't belong to any group
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, /* user */ null, /* groups */ new HashSet<>(),
                null, null, adminOwnership, acl));
        user.addPermission(eventReadPermission);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        final Set<String> permissionSet = new HashSet<>();
        permissionSet.add("!" + DefaultActions.READ.name());
        acl.setPermissions(userTenant, permissionSet);
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        // User ownership shall NOT imply permissions; the revoking ACL still takes precedence
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                ownership, acl));
        // now add "public read" permission to ACL:
        acl.addPermission(null, DefaultActions.READ.name());
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, /* user */ null, /* groups */ new HashSet<>(),
                null, null, adminOwnership, acl));
        // now deny "public read" permission in ACL which is expected to supersede the granting from above:
        acl.denyPermission(null, DefaultActions.READ.name());
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, /* user */ null, /* groups */ new HashSet<>(),
                null, null, adminOwnership, acl));
    }
    
    @Test
    public void testDirectPermission() {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        user.addPermission(eventReadPermission);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
    }
    
    @Test
    public void testRole() {
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        final Role globalRole = new Role(globalRoleDefinition);
        user.addRole(globalRole);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        user.removeRole(globalRole);
        user.addRole(new Role(globalRoleDefinition, this.userTenant, /* user qualifier */ null));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                adminOwnership, acl));
        Ownership testOwnership = new Ownership(adminUser, userTenant);
        assertTrue(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                testOwnership, acl));
        assertFalse(PermissionChecker.isPermitted(eventReadPermission, user, tenants, null, null,
                null, acl));
    }
    
    @Test
    public void testRoleAssociatedToGroup() {
        final RoleDefinition roleDefinition = new RoleDefinitionImpl(UUID.randomUUID(), "some_role",
                Arrays.asList(WildcardPermission.builder().withTypes(type1).build()));
        final Ownership foo = new Ownership(null, userTenant);
        Supplier<Boolean> permissionCheckGranted = () -> PermissionChecker.isPermitted(
                WildcardPermission.builder().withTypes(type1).withActions(DefaultActions.READ).withIds("abc").build(),
                user, tenants, null, null, foo, null);
        Supplier<Boolean> permissionCheckNotGranted = () -> PermissionChecker.isPermitted(
                WildcardPermission.builder().withTypes(type2).withActions(DefaultActions.READ).withIds("abc").build(),
                user, tenants, null, null, foo, null);
        assertFalse(permissionCheckGranted.get());
        userTenant.put(roleDefinition, false);
        assertTrue(permissionCheckGranted.get());
        assertFalse(permissionCheckNotGranted.get());
        userTenant.remove(roleDefinition);
        assertFalse(permissionCheckGranted.get());
        userTenant.put(roleDefinition, true);
        assertTrue(permissionCheckGranted.get());
        assertFalse(permissionCheckNotGranted.get());
    }

    @Test
    public void testMetaPermissionCheck() {
        final WildcardPermission allPermission = WildcardPermission.builder().build();
        final WildcardPermission singleTypePermission = type1.getPermission();
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(singleTypePermission));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(singleTypePermission, allPermission));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(singleTypePermission, type1.getPermission()));
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(singleTypePermission, type2.getPermission()));
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(singleTypePermission,
                type1.getPermission(DefaultActions.READ)));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(singleTypePermission,
                type1.getPermission(DefaultActions.READ, DefaultActions.UPDATE)));

        final WildcardPermission combinedTypePermission = WildcardPermission.builder().withTypes(type1, type2).build();
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission));
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission, type1.getPermission()));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission, allPermission));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission, combinedTypePermission));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission, type1.getPermission(),
                type2.getPermission()));
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission, type1.getPermission(),
                type2.getPermission(DefaultActions.READ)));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypePermission, type1.getPermission(),
                type2.getPermission(DefaultActions.READ, DefaultActions.DELETE)));

        final WildcardPermission combinedTypeWithDistinctActionPermission = WildcardPermission.builder()
                .withTypes(type1, type2).withActions(DefaultActions.READ).build();
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission));
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission()));
        assertTrue(
                checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission, allPermission));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(), type2.getPermission()));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(DefaultActions.READ), type2.getPermission()));
        assertFalse(checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(DefaultActions.READ), type2.getPermission(DefaultActions.DELETE)));
        assertTrue(checkMetaPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(DefaultActions.READ), type2.getPermission(DefaultActions.READ)));
    }

    private boolean checkMetaPermissionWithGrantedUserPermissions(WildcardPermission permissionToCheck,
            WildcardPermission... grantedPermissions) {
        for (WildcardPermission p : grantedPermissions) {
            user.addPermission(p);
        }
        boolean result = PermissionChecker.checkMetaPermission(permissionToCheck, allHasPermissions, user, null, null);
        for (WildcardPermission p : grantedPermissions) {
            user.removePermission(p);
        }
        return result;
    }

    @Test
    public void testAnyPermissionCheck() {
        final WildcardPermission allPermission = WildcardPermission.builder().build();
        final WildcardPermission singleTypePermission = type1.getPermission();
        assertFalse(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission, allPermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission, type1.getPermission()));
        assertFalse(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission, type2.getPermission()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission,
                type1.getPermission(DefaultActions.READ)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission,
                type1.getPermission(DefaultActions.READ, DefaultActions.UPDATE)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission,
                WildcardPermission.builder().withActions(DefaultActions.READ, DefaultActions.UPDATE).build()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermission,
                WildcardPermission.builder().withActions(DefaultActions.READ, DefaultActions.UPDATE)
                        .withIds(WildcardPermission.WILDCARD_TOKEN).build()));
        
        final WildcardPermission singleTypePermissionWithAction = type1.getPermission(DefaultActions.READ);
        assertFalse(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction, allPermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction, type1.getPermission()));
        assertFalse(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction, type2.getPermission()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction,
                type1.getPermission(DefaultActions.READ)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction,
                type1.getPermission(DefaultActions.READ, DefaultActions.UPDATE)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction,
                WildcardPermission.builder().withActions(DefaultActions.READ, DefaultActions.UPDATE).build()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(singleTypePermissionWithAction,
                WildcardPermission.builder().withActions(DefaultActions.READ, DefaultActions.UPDATE)
                .withIds(WildcardPermission.WILDCARD_TOKEN).build()));

        final WildcardPermission combinedTypePermission = WildcardPermission.builder().withTypes(type1, type2).build();
        assertFalse(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission, type1.getPermission()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission, allPermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission, combinedTypePermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission, type1.getPermission(),
                type2.getPermission()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission,
                type1.getPermission(DefaultActions.READ)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission,
                type1.getPermission(DefaultActions.UPDATE), type2.getPermission(DefaultActions.READ)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission,
                type1.getPermission(DefaultActions.READ, DefaultActions.UPDATE)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission,
                WildcardPermission.builder().withActions(DefaultActions.READ, DefaultActions.UPDATE).build()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypePermission,
                WildcardPermission.builder().withActions(DefaultActions.READ, DefaultActions.UPDATE)
                .withIds(WildcardPermission.WILDCARD_TOKEN).build()));

        final WildcardPermission combinedTypeWithDistinctActionPermission = WildcardPermission.builder()
                .withTypes(type1, type2).withActions(DefaultActions.READ).build();
        assertFalse(checkAnyPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission()));
        assertTrue(
                checkAnyPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission, allPermission));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(), type2.getPermission()));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(DefaultActions.READ)));
        assertTrue(checkAnyPermissionWithGrantedUserPermissions(combinedTypeWithDistinctActionPermission,
                type1.getPermission(DefaultActions.READ), type2.getPermission(DefaultActions.DELETE)));
    }

    private boolean checkAnyPermissionWithGrantedUserPermissions(WildcardPermission permissionToCheck,
            WildcardPermission... grantedPermissions) {
        for (WildcardPermission p : grantedPermissions) {
            user.addPermission(p);
        }
        boolean result = PermissionChecker.hasUserAnyPermission(permissionToCheck, allHasPermissions, user, null, null);
        for (WildcardPermission p : grantedPermissions) {
            user.removePermission(p);
        }
        return result;
    }
}