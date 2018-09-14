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

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
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
        DB db = service.getDB();
        db.getCollection(CollectionNames.USERS.name()).drop();
        db.getCollection(CollectionNames.USER_GROUPS.name()).drop();
        db.getCollection(CollectionNames.ROLES.name()).drop();
        db.getCollection(CollectionNames.SETTINGS.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        userStore = new UserStoreImpl(DEFAULT_TENANT_NAME);
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
        final RoleImpl testRole = new RoleImpl(testRoleDefinition);
        userStore.addRoleForUser("me", testRole);
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        assertTrue(Util.contains(store2.getUserByName("me").getRoles(), testRole));
    }

    @Test
    public void roleWithQualifiersTest() throws UserManagementException, UserGroupManagementException {
        UserGroup userDefaultTenant = userStore.createUserGroup(UUID.randomUUID(), "me-tenant");
        User meUser = userStore.createUser("me", "me@sap.com", userDefaultTenant);
        RoleDefinition testRoleDefinition = userStore.createRoleDefinition(UUID.randomUUID(), "testRole", Collections.emptySet());
        final RoleImpl testRole = new RoleImpl(testRoleDefinition, userDefaultTenant, meUser);
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
        userStore.addPermissionForUser("me", new WildcardPermission("a:b:c", /* case sensitive */ true));
        UserStoreImpl store2 = new UserStoreImpl(DEFAULT_TENANT_NAME);
        assertTrue(store2.getUserByName("me").hasPermission(new WildcardPermission("a:b:c", /* case sensitive */ true)));
    }

}
