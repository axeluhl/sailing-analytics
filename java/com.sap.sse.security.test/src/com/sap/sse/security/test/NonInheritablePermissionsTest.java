package com.sap.sse.security.test;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.security.AbstractCompositeAuthorizingRealm;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionChecker.AclResolver;
import com.sap.sse.security.shared.PredefinedRoles;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.HasPermissionsImpl;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class NonInheritablePermissionsTest {
    private final AclResolver<AccessControlList, Ownership> noopAclResolver = new AclResolver<AccessControlList, Ownership>() {
        @Override
        public Iterable<AccessControlList> resolveDenyingAclsAndCheckIfAnyMatches(Ownership ownershipOrNull,
                String type, Iterable<String> objectIdentifiersAsStringOrNull, Predicate<AccessControlList> filterCondition,
                Iterable<AccessControlList> allAclsForTypeAndObjectIdsOrNull) {
            return Collections.emptySet(); // assuming an empty ACL set
        }
    };
    private User user;
    private UserStore userStore;
    private AccessControlStore accessControlStore;
    private HasPermissions type1 = new HasPermissionsImpl("DEMO", DefaultActions.READ, DefaultActions.UPDATE);
    private HasPermissions type2 = new HasPermissionsImpl("TEST", DefaultActions.READ, DefaultActions.DELETE);
    private Iterable<HasPermissions> allHasPermissions = Arrays.asList(type1, type2);
    
    @Before
    public void setUp() throws UserGroupManagementException, UserManagementException {
        final String adminTenantName = "admin-tenant";
        userStore = new UserStoreImpl(adminTenantName);
        userStore.ensureDefaultRolesExist();
        userStore.ensureServerGroupExists();
        accessControlStore = new AccessControlStoreImpl(userStore);
        AbstractCompositeAuthorizingRealm.setTestStores(userStore, accessControlStore);
        if (userStore.getUserByName("test") != null) {
            userStore.deleteUser("test");
        }
        user = userStore.createUser("test", "test@sap.com");
    }
    
    @Test
    public void testSubscriptionPermissionHandDown() throws UserManagementException {
        final RoleDefinition mediaEditorRoleDefinition = userStore.getRoleDefinition(PredefinedRoles.mediaeditor.getId());
        final WildcardPermission permissionImpliedByRole = mediaEditorRoleDefinition.getPermissions().iterator().next();
        final Role role = new Role(mediaEditorRoleDefinition, null, null);
        role.setOriginatesFromSubscription(true);
        user.addRole(role);
        assertFalse(PermissionChecker.checkMetaPermission(permissionImpliedByRole, allHasPermissions, user, null, null, noopAclResolver));
    }
}