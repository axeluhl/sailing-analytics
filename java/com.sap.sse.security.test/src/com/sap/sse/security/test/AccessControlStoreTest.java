package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
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
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

/**
 * Tests that the MongoDB persistence is always in sync with the {@link UserStore}.
 */
public class AccessControlStoreTest {
    private static final String DEFAULT_TENANT_NAME = "TestDefaultTenant";
    private final QualifiedObjectIdentifier testIdAsString = new QualifiedObjectIdentifierImpl("Test", "test");
    private final String testDisplayName = "testDN";
    private final User testOwner = new UserImpl("admin", "admin@sapsailing.com", new UserGroupImpl(UUID.randomUUID(), "admin-tenant"),
            /* userGroupProvider */ null);
    private final UserGroup testTenantOwner = new UserGroupImpl(UUID.randomUUID(), "test-tenant");
    private final UUID testRoleId = UUID.randomUUID();

    private UserStore userStore;
    private AccessControlStore accessControlStore;

    @Before
    public void setUp() throws UnknownHostException, MongoException, UserGroupManagementException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        DB db = service.getDB();
        db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name()).drop();
        db.getCollection(CollectionNames.OWNERSHIPS.name()).drop();
        db.getCollection(CollectionNames.ROLES.name()).drop();
        newStores();
    }

    private void newStores() {
        try {
            userStore = new UserStoreImpl(DEFAULT_TENANT_NAME);
        } catch (UserGroupManagementException | UserManagementException e) {
            throw new RuntimeException(e);
        }
        accessControlStore = new AccessControlStoreImpl(userStore);
    }

    @Test
    public void testCreateAccessControlList() throws UserGroupManagementException {
        accessControlStore.createAccessControlList(testIdAsString, testDisplayName);
        assertNotNull(accessControlStore.getAccessControlList(testIdAsString));
        newStores();
        assertNotNull(accessControlStore.getAccessControlList(testIdAsString));
    }
    
    @Test
    public void testDeleteAccessControlList() throws UserGroupManagementException {
        accessControlStore.createAccessControlList(testIdAsString, testDisplayName);
        accessControlStore.removeAccessControlList(testIdAsString);
        assertNull(accessControlStore.getAccessControlList(testIdAsString));
        newStores();
        assertNull(accessControlStore.getAccessControlList(testIdAsString));
    }
    
    @Test
    public void testCreateOwnership() throws UserGroupManagementException {
        accessControlStore.createOwnership(testIdAsString, testOwner, testTenantOwner, testDisplayName);
        assertNotNull(accessControlStore.getOwnership(testIdAsString));
        newStores();
        assertNotNull(accessControlStore.getOwnership(testIdAsString));
    }
    
    @Test
    public void testDeleteOwnership() throws UserGroupManagementException {
        accessControlStore.createOwnership(testIdAsString, testOwner, testTenantOwner, testDisplayName);
        accessControlStore.removeOwnership(testIdAsString);
        // expecting to fall back to default tenant ownership
        final OwnershipAnnotation defaultOwnership = accessControlStore.getOwnership(testIdAsString);
        assertDefaultOwnership(defaultOwnership);
        newStores();
        assertDefaultOwnership(accessControlStore.getOwnership(testIdAsString));
    }

    private void assertDefaultOwnership(final OwnershipAnnotation defaultOwnershipToCheck) {
        assertNull(defaultOwnershipToCheck.getAnnotation().getUserOwner());
        assertNotNull(defaultOwnershipToCheck.getAnnotation().getTenantOwner());
        assertEquals(DEFAULT_TENANT_NAME, defaultOwnershipToCheck.getAnnotation().getTenantOwner().getName());
        assertNull(defaultOwnershipToCheck.getDisplayNameOfAnnotatedObject());
    }
    
    @Test
    public void testCreateRole() throws UserGroupManagementException {
        userStore.createRoleDefinition(testRoleId, testDisplayName, new HashSet<WildcardPermission>());
        assertNotNull(userStore.getRoleDefinition(testRoleId));
        newStores();
        assertNotNull(userStore.getRoleDefinition(testRoleId));
    }
    
    @Test
    public void testDeleteRole() throws UserGroupManagementException {
        final RoleDefinition role = userStore.createRoleDefinition(testRoleId, testDisplayName, new HashSet<WildcardPermission>());
        userStore.removeRoleDefinition(role);
        assertNull(userStore.getRoleDefinition(testRoleId));
        newStores();
        assertNull(userStore.getRoleDefinition(testRoleId));
    }
}
