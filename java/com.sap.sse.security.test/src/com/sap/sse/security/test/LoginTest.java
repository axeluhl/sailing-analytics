package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

public class LoginTest {
    private static final String DEFAULT_TENANT_NAME = "TestDefaultTenant";
    private UserStoreImpl userStore;
    private AccessControlStore accessControlStore;

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
        userStore.ensureDefaultTenantExists();
        accessControlStore = new AccessControlStoreImpl(userStore);
        
        Activator.setTestStores(userStore, accessControlStore);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // to enable shiro to find classes from com.sap.sse.security
        Activator.setSecurityService(new SecurityServiceImpl(userStore, accessControlStore));
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
        userStore.createUser("me", "me@sap.com", new UserGroupImpl(UUID.randomUUID(), "me-tenant"));
        RoleDefinition testRoleDefinition = userStore.createRoleDefinition(UUID.randomUUID(), "testRole", Collections.emptySet());
        final Role testRole = new Role(testRoleDefinition);
        userStore.addRoleForUser("me", testRole);
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        assertTrue(Util.contains(store2.getUserByName("me").getRoles(), testRole));
    }

    @Test
    public void roleWithQualifiersTest() throws UserManagementException, UserGroupManagementException {
        UserGroupImpl userDefaultTenant = userStore.createUserGroup(UUID.randomUUID(), "me-tenant");
        User meUser = userStore.createUser("me", "me@sap.com", userDefaultTenant);
        RoleDefinition testRoleDefinition = userStore.createRoleDefinition(UUID.randomUUID(), "testRole", Collections.emptySet());
        final Role testRole = new Role(testRoleDefinition, userDefaultTenant, meUser);
        userStore.addRoleForUser("me", testRole);
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        assertTrue(Util.contains(store2.getUserByName("me").getRoles(), testRole));
        Role role2 = store2.getUserByName("me").getRoles().iterator().next();
        assertSame(store2.getUserGroupByName("me-tenant"), role2.getQualifiedForTenant());
        assertSame(store2.getUserByName("me"), role2.getQualifiedForUser());
    }

    @Test
    public void permissionsTest() throws UserManagementException, UserGroupManagementException {
        userStore.createUser("me", "me@sap.com", new UserGroupImpl(UUID.randomUUID(), "me-tenant"));
        userStore.addPermissionForUser("me", new WildcardPermission("a:b:c"));
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        User allUser = userStore.getUserByName(SecurityService.ALL_USERNAME);
        User user = store2.getUserByName("me");
        assertTrue(PermissionChecker.isPermitted(new WildcardPermission("a:b:c"), user, allUser, null, null));
    }

}
