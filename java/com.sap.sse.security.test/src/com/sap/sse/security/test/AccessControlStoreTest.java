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
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

/**
 * Tests that the MongoDB persistence is always in sync with the {@link UserStore}.
 */
public class AccessControlStoreTest {
    private final String testIdAsString = "test";
    private final String testDisplayName = "testDN";
    private final String testOwner = "admin";
    private final UUID testTenantOwner = UUID.randomUUID();
    private final UUID testRoleId = UUID.randomUUID();

    private AccessControlStoreImpl store;

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        DB db = service.getDB();
        db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name()).drop();
        db.getCollection(CollectionNames.OWNERSHIPS.name()).drop();
        db.getCollection(CollectionNames.ROLES.name()).drop();
        newStore();
    }

    private void newStore() {
        store = new AccessControlStoreImpl();
    }

    @Test
    public void testCreateAccessControlList() {
        store.createAccessControlList(testIdAsString, testDisplayName);
        assertNotNull(store.getAccessControlList(testIdAsString));

        newStore();
        assertNotNull(store.getAccessControlList(testIdAsString));
    }
    
    @Test
    public void testDeleteAccessControlList() {
        store.createAccessControlList(testIdAsString, testDisplayName);
        store.removeAccessControlList(testIdAsString);
        assertNull(store.getAccessControlList(testIdAsString));

        newStore();
        assertNull(store.getAccessControlList(testIdAsString));
    }
    
    @Test
    public void testCreateOwnership() {
        store.createOwnership(testIdAsString, testOwner, testTenantOwner, testDisplayName);
        assertNotNull(store.getOwnership(testIdAsString));

        newStore();
        assertNotNull(store.getOwnership(testIdAsString));
    }
    
    @Test
    public void testDeleteOwnership() {
        store.createOwnership(testIdAsString, testOwner, testTenantOwner, testDisplayName);
        store.removeOwnership(testIdAsString);
        assertNull(store.getOwnership(testIdAsString));

        newStore();
        assertNull(store.getOwnership(testIdAsString));
    }
    
    @Test
    public void testCreateRole() {
        store.createRole(testRoleId, testDisplayName, new HashSet<WildcardPermission>());
        assertNotNull(store.getRole(testRoleId));

        newStore();
        assertNotNull(store.getRole(testRoleId));
    }
    
    @Test
    public void testDeleteRole() {
        store.createRole(testRoleId, testDisplayName, new HashSet<WildcardPermission>());
        store.removeRole(testRoleId);
        assertNull(store.getRole(testRoleId));

        newStore();
        assertNull(store.getRole(testRoleId));
    }
}
