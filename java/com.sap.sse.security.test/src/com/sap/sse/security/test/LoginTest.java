package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.shared.AdminRole;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

public class LoginTest {
    private static final String DEFAULT_TENANT_NAME = "TestDefaultTenant";
    private UserStoreImpl userStore;
    private AccessControlStore accessControlStore;
    private SecurityService securityService;

    @Before
    public void setUp() throws UnknownHostException, MongoException, UserGroupManagementException, UserManagementException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        MongoDatabase db = service.getDB();
        db.getCollection(CollectionNames.USERS.name()).drop();
        db.getCollection(CollectionNames.USER_GROUPS.name()).drop();
        db.getCollection(CollectionNames.ROLES.name()).drop();
        db.getCollection(CollectionNames.SETTINGS.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        db.getCollection(com.sap.sse.security.persistence.impl.CollectionNames.SESSIONS.name()).drop();
        userStore = new UserStoreImpl(DEFAULT_TENANT_NAME);
        userStore.ensureDefaultRolesExist();
        userStore.ensureServerGroupExists();
        accessControlStore = new AccessControlStoreImpl(userStore);
        Activator.setTestStores(userStore, accessControlStore);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // to enable shiro to find classes from com.sap.sse.security
        securityService = new SecurityServiceImpl(/* mailServiceTracker */ null, userStore, accessControlStore, SecuredSecurityTypes::getAllInstances);
        Activator.setSecurityService(securityService);
    }

    @Test
    public void testDeleteUser() throws UserManagementException, MailException, UserGroupManagementException {
        final String username = "TheNewUser";
        final String specialUserGroupName1 = "TheSpecialUserGroup1";
        final String specialUserGroupName2 = "TheSpecialUserGroup2";
        final User user = securityService.createSimpleUser(username, "u@a.b", "Humba", "The New User", /* company */ null, /* locale */ null, /* validationBaseURL */ null, /* owning group */ null);
        final UserGroup defaultUserGroup = securityService.getUserGroupByName(username+SecurityService.TENANT_SUFFIX);
        final UserGroup specialUserGroup1 = securityService.createUserGroup(UUID.randomUUID(), specialUserGroupName1);
        final UserGroup specialUserGroup2 = securityService.createUserGroup(UUID.randomUUID(), specialUserGroupName2);
        securityService.addUserToUserGroup(specialUserGroup1, user);
        securityService.addUserToUserGroup(specialUserGroup2, user);
        assertNotNull(defaultUserGroup);
        assertTrue(Util.contains(defaultUserGroup.getUsers(), user));
        securityService.deleteUser(username);
        assertNull(securityService.getUserByName(username));
        assertFalse(Util.contains(specialUserGroup1.getUsers(), user));
        assertFalse(Util.contains(specialUserGroup2.getUsers(), user));
    }

    @Test
    public void testAclAnonUserGroup() throws UserManagementException, MailException, UserGroupManagementException {
        final String username = "TheNewUser";
        securityService.createSimpleUser(username, "u@a.b", "Humba", username, /* company */ null,
                /* locale */ null, /* validationBaseURL */ null, /* owning group */ null);
        final UserGroup defaultUserGroup = securityService.getUserGroupByName(username + SecurityService.TENANT_SUFFIX);
        Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
        permissionMap.put(defaultUserGroup, new HashSet<>(Arrays.asList(new String[] { "!READ", "UPDATE" })));
        permissionMap.put(null, new HashSet<>(Arrays.asList(new String[] { "!READ", "UPDATE" })));
        AccessControlList acl = securityService.overrideAccessControlList(
                QualifiedObjectIdentifierImpl.fromDBWithoutEscaping("someid/more"), permissionMap);
        Map<UserGroup, Set<String>> result = acl.getActionsByUserGroup();
        Assert.assertThat(result.get(defaultUserGroup), Matchers.contains("!READ", "UPDATE"));
        Assert.assertThat(result.get(null), Matchers.contains("UPDATE"));
    }
    
    @Test
    public void testDenialInAclAffectsMetaPermissionCheck() throws UserManagementException, MailException, UserGroupManagementException {
        final WithQualifiedObjectIdentifier my = new WithQualifiedObjectIdentifier() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getName() {
                return "my";
            }
            
            @Override
            public HasPermissions getPermissionType() {
                return SecuredSecurityTypes.SERVER;
            }
            
            @Override
            public QualifiedObjectIdentifier getIdentifier() {
                return getPermissionType().getQualifiedObjectIdentifier(new TypeRelativeObjectIdentifier(getName()));
            }
        };
        securityService.initialize(); // create admin user/tenant
        final String username = "TheNewUser";
        final String password = "Humba";
        final User admin = securityService.getUserByName("admin");
        final RoleDefinition adminRoleDefinition = securityService.getOrCreateRoleDefinitionFromPrototype(AdminRole.getInstance());
        final UserGroup adminTenant = securityService.getUserGroupByName(admin.getName()+SecurityService.TENANT_SUFFIX);
        securityService.createSimpleUser(username, "u@a.b", password, username, /* company */ null,
                /* locale */ null, /* validationBaseURL */ null, /* owning group */ null);
        final UserGroup defaultUserGroup = securityService.getUserGroupByName(username + SecurityService.TENANT_SUFFIX);
        final QualifiedObjectIdentifier myId = my.getIdentifier();
        // grant admin role to user unqualified, implying READ on all objects including the "my" SERVER
        securityService.addRoleForUser(username, new Role(securityService.getOrCreateRoleDefinitionFromPrototype(AdminRole.getInstance())));
        securityService.login(username, password);
        securityService.setOwnership(myId, admin, adminTenant);
        // check explicit permission:
        assertTrue(securityService.hasCurrentUserExplicitPermissions(my, ServerActions.READ_REPLICATOR));
        // check for meta-permission SERVER:*:*
        assertTrue(securityService.hasCurrentUserMetaPermissionWithOwnershipLookup(
                WildcardPermission.builder().withTypes(SecuredSecurityTypes.SERVER).build()));
        // now add an ACL to the "my" server that disallows READ_REPLICATOR for the user's default group
        Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
        permissionMap.put(defaultUserGroup, new HashSet<>(Arrays.asList(new String[] { "!READ_REPLICATOR" })));
        securityService.overrideAccessControlList(myId, permissionMap);
        // now the user is expected to have lost the READ_REPLICATOR permission on "my"
        assertFalse(securityService.hasCurrentUserExplicitPermissions(my, ServerActions.READ_REPLICATOR));
        // and therefore no meta-permission (permission to grant) for SERVER:*:* anymore:
        assertFalse(securityService.hasCurrentUserMetaPermissionWithOwnershipLookup(
                WildcardPermission.builder().withTypes(SecuredSecurityTypes.SERVER).build()));
        // and therefore no meta-permission (permission to grant) for SERVER:*:my anymore:
        assertFalse(securityService.hasCurrentUserMetaPermissionWithOwnershipLookup(
                WildcardPermission.builder().withTypes(SecuredSecurityTypes.SERVER).withIds(myId.getTypeRelativeObjectIdentifier()).build()));
        // and therefore no meta-permission (permission to grant) role "admin" qualified to the default user group:
        assertFalse(securityService.hasCurrentUserMetaPermissionsOfRoleDefinitionWithQualification(
                adminRoleDefinition, new Ownership(/* user */ null, adminTenant)));
    }
    
    @Test
    public void testGetUser() {
        assertNotNull("Subject should not be null: ", SecurityUtils.getSubject());
    }
    
    @Test
    public void setPreferencesTest() throws UserGroupManagementException, UserManagementException {
        userStore.setPreference("me", "key", "value");
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        assertEquals("value", store2.getPreference("me", "key"));
    }

    @Test
    public void setAndUnsetPreferencesTest() throws UserGroupManagementException, UserManagementException {
        userStore.setPreference("me", "key", "value");
        userStore.unsetPreference("me", "key");
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        assertNull(store2.getPreference("me", "key"));
    }

    @Test
    public void rolesTest() throws UserManagementException, UserGroupManagementException {
        userStore.createUser("me", "me@sap.com");
        RoleDefinition testRoleDefinition = userStore.createRoleDefinition(UUID.randomUUID(), "testRole", Collections.emptySet());
        final Role testRole = new Role(testRoleDefinition);
        userStore.addRoleForUser("me", testRole);
        UserStoreImpl store2 = createAndLoadUserStore();
        assertTrue(Util.contains(store2.getUserByName("me").getRoles(), testRole));
    }

    @Test
    public void roleWithQualifiersTest() throws UserManagementException, UserGroupManagementException {
        UserGroupImpl userDefaultTenant = userStore.createUserGroup(UUID.randomUUID(), "me-tenant");
        User meUser = userStore.createUser("me", "me@sap.com");
        RoleDefinition testRoleDefinition = userStore.createRoleDefinition(UUID.randomUUID(), "testRole", Collections.emptySet());
        final Role testRole = new Role(testRoleDefinition, userDefaultTenant, meUser);
        userStore.addRoleForUser("me", testRole);
        UserStoreImpl store2 = createAndLoadUserStore();
        assertTrue(Util.contains(store2.getUserByName("me").getRoles(), testRole));
        Role role2 = store2.getUserByName("me").getRoles().iterator().next();
        assertSame(store2.getUserGroupByName("me-tenant"), role2.getQualifiedForTenant());
        assertSame(store2.getUserByName("me"), role2.getQualifiedForUser());
    }

    @Test
    public void permissionsTest() throws UserManagementException, UserGroupManagementException {
        userStore.createUser("me", "me@sap.com");
        userStore.addPermissionForUser("me", new WildcardPermission("a:b:c"));
        UserStoreImpl store2 = createAndLoadUserStore();
        User allUser = userStore.getUserByName(SecurityService.ALL_USERNAME);
        User user = store2.getUserByName("me");
        assertTrue(PermissionChecker.isPermitted(new WildcardPermission("a:b:c"), user, allUser, null, null));
    }

    private UserStoreImpl createAndLoadUserStore() throws UserGroupManagementException, UserManagementException {
        final UserStoreImpl store = new UserStoreImpl(DEFAULT_TENANT_NAME);
        store.loadAndMigrateUsers();
        return store;
    }

}
