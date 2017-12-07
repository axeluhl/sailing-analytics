package com.sap.sse.security.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.TenantImpl;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

/**
 * Tests that the MongoDB persistence is always in sync with the {@link UserStore}.
 */
public class AccessControlStoreTest {
    private final String testIdAsString = "test";
    private final String testDisplayName = "testDN";
    private final User testOwner = new UserImpl("admin", "admin@sapsailing.com", new TenantImpl(UUID.randomUUID(), "admin-tenant"));
    private final Tenant testTenantOwner = new TenantImpl(UUID.randomUUID(), "test-tenant");
    private final UUID testRoleId = UUID.randomUUID();

    private UserStore userStore;
    private AccessControlStore accessControlStore;

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        DB db = service.getDB();
        db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name()).drop();
        db.getCollection(CollectionNames.OWNERSHIPS.name()).drop();
        db.getCollection(CollectionNames.ROLES.name()).drop();
        newStores();
    }

    private void newStores() {
        userStore = new UserStoreImpl();
        accessControlStore = new AccessControlStoreImpl(userStore);
    }

    @Test
    public void testCreateAccessControlList() {
        accessControlStore.createAccessControlList(testIdAsString, testDisplayName);
        assertNotNull(accessControlStore.getAccessControlList(testIdAsString));

        newStores();
        assertNotNull(accessControlStore.getAccessControlList(testIdAsString));
    }
    
    @Test
    public void testDeleteAccessControlList() {
        accessControlStore.createAccessControlList(testIdAsString, testDisplayName);
        accessControlStore.removeAccessControlList(testIdAsString);
        assertNull(accessControlStore.getAccessControlList(testIdAsString));

        newStores();
        assertNull(accessControlStore.getAccessControlList(testIdAsString));
    }
    
    @Test
    public void testCreateOwnership() {
        accessControlStore.createOwnership(testIdAsString, testOwner, testTenantOwner, testDisplayName);
        assertNotNull(accessControlStore.getOwnership(testIdAsString));

        newStores();
        assertNotNull(accessControlStore.getOwnership(testIdAsString));
    }
    
    @Test
    public void testDeleteOwnership() {
        accessControlStore.createOwnership(testIdAsString, testOwner, testTenantOwner, testDisplayName);
        accessControlStore.removeOwnership(testIdAsString);
        assertNull(accessControlStore.getOwnership(testIdAsString));

        newStores();
        assertNull(accessControlStore.getOwnership(testIdAsString));
    }
    
    @Test
    public void testCreateRole() {
        userStore.createRole(testRoleId, testDisplayName, new HashSet<WildcardPermission>());
        assertNotNull(userStore.getRole(testRoleId));

        newStores();
        assertNotNull(userStore.getRole(testRoleId));
    }
    
    @Test
    public void testDeleteRole() {
        userStore.createRole(testRoleId, testDisplayName, new HashSet<WildcardPermission>());
        userStore.removeRole(testRoleId);
        assertNull(userStore.getRole(testRoleId));

        newStores();
        assertNull(userStore.getRole(testRoleId));
    }
}
