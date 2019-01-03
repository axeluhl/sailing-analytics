package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.apache.shiro.SecurityUtils;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

public class LoginTest {
    private UserStoreImpl store;

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        MongoDatabase db = service.getDB();
        db.getCollection(CollectionNames.USERS.name()).drop();
        db.getCollection(CollectionNames.SETTINGS.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        store = new UserStoreImpl();
        UsernamePasswordRealm.setTestUserStore(store);
        Activator.setTestUserStore(store);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // to enable shiro to find classes from com.sap.sse.security
        Activator.setSecurityService(new SecurityServiceImpl(store));
    }

    @Test
    public void testGetUser() {
        assertNotNull("Subject should not be null: ", SecurityUtils.getSubject());
    }
    
    @Test
    public void setPreferencesTest() {
        store.setPreference("me", "key", "value");
        UserStoreImpl store2 = new UserStoreImpl();
        assertEquals("value", store2.getPreference("me", "key"));
    }

    @Test
    public void setAndUnsetPreferencesTest() {
        store.setPreference("me", "key", "value");
        store.unsetPreference("me", "key");
        UserStoreImpl store2 = new UserStoreImpl();
        assertNull(store2.getPreference("me", "key"));
    }

    @Test
    public void rolesTest() throws UserManagementException {
        store.createUser("me", "me@sap.com");
        store.addRoleForUser("me", "testrole");
        UserStoreImpl store2 = new UserStoreImpl();
        assertTrue(Util.contains(store2.getUserByName("me").getRoles(), "testrole"));
    }

    @Test
    public void permissionsTest() throws UserManagementException {
        store.createUser("me", "me@sap.com");
        store.addPermissionForUser("me", "a:b:c");
        UserStoreImpl store2 = new UserStoreImpl();
        assertTrue(store2.getUserByName("me").hasPermission("a:b:c"));
    }

}
