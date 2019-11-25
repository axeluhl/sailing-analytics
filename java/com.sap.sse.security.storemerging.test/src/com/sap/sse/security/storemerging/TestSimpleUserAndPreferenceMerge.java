package com.sap.sse.security.storemerging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.storemerging.test.MongoDBFiller;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

public class TestSimpleUserAndPreferenceMerge {
    private final static String importSourceMongoDbUri = "mongodb://localhost/"+UUID.randomUUID();
    private final static String defaultCreationGroupNameForSource = "dummy-default-creation-group-for-source";
    private final static String defaultCreationGroupNameForTarget = "dummy-default-creation-group-for-target";
    private MongoDBConfiguration cfgForSource;
    private MongoDatabase targetDb;
    private MongoDBConfiguration cfgForTarget;

    @Before
    public void setUp() throws IOException {
        cfgForTarget = MongoDBConfiguration.getDefaultTestConfiguration();
        targetDb = cfgForTarget.getService().getDB();
        fill("target1", targetDb);
        cfgForSource = new MongoDBConfiguration(new MongoClientURI(importSourceMongoDbUri));
        fill("source1", cfgForSource.getService().getDB());
    }
    
    @After
    public void tearDown() {
        cfgForSource.getService().getDB().drop();
    }

    private void fill(final String variant, final MongoDatabase db) throws IOException {
        for (final CollectionNames collectionName : new CollectionNames[] { CollectionNames.USERS,
                CollectionNames.USER_GROUPS, CollectionNames.OWNERSHIPS, CollectionNames.ACCESS_CONTROL_LISTS,
                CollectionNames.PREFERENCES }) {
            fill(collectionName, variant, db);
        }
    }

    private void fill(final CollectionNames collectionName, final String variant, final MongoDatabase db)
            throws IOException {
        final MongoDBFiller filler = new MongoDBFiller();
        final MongoCollection<Document> collection = db.getCollection(collectionName.name());
        collection.drop();
        filler.fill(collection, "/resources/"+collectionName.name()+"_"+variant+".json");
    }
    
    @Test
    public void testGroupIdentity() {
        // TODO test SecurityStoreMerger.considerGroupsIdentical...
    }
    
    @Test
    public void testImportFromSource1ToTarget1() throws UserGroupManagementException, UserManagementException {
        final SecurityStoreMerger merger = new SecurityStoreMerger(cfgForTarget, defaultCreationGroupNameForTarget);
        final UserStore targetUserStore = merger.getTargetUserStore();
        assertNotNull(targetUserStore.getUserByName("admin"));
        assertNotNull(targetUserStore.getUserByName("<all>"));
        assertNotNull(targetUserStore.getUserByName("uhl"));
        assertNotNull(targetUserStore.getUserByName("uhl2"));
        assertNotNull(targetUserStore.getUserByName("axel.uhl"));
        assertNotNull(targetUserStore.getUserByName("axel"));
        assertNotNull(targetUserStore.getUserByName("YCaT"));
        assertNotNull(targetUserStore.getUserByName("YCaT-member1"));
        assertNotNull(targetUserStore.getUserByName("YCaT-member2"));
        assertNotNull(targetUserStore.getUserByName("uhl3"));
        assertNull(targetUserStore.getUserByName("uhl4"));
        assertEquals(1, Util.size(targetUserStore.getUserGroupByName("uhl-tenant").getUsers()));
        assertSame(targetUserStore.getUserByName("uhl"), targetUserStore.getUserGroupByName("uhl-tenant").getUsers().iterator().next());
        final AccessControlStore targetAccessControlStore = merger.getTargetAccessControlStore();
        final Pair<UserStore, AccessControlStore> sourceStores = merger.importStores(cfgForSource, defaultCreationGroupNameForSource);
        final UserStore sourceUserStore = sourceStores.getA();
        final AccessControlStore sourceAccessControlStore = sourceStores.getB();
        assertNotNull(sourceUserStore.getUserByName("admin"));
        assertNotNull(sourceUserStore.getUserByName("<all>"));
        assertNotNull(sourceUserStore.getUserByName("uhl"));
        assertNotNull(sourceUserStore.getUserByName("uhl3"));
        assertNotNull(sourceUserStore.getUserByName("axel.uhl"));
        assertNotNull(sourceUserStore.getUserByName("uhl4"));
        // check result in target user store:
        assertNotNull(targetUserStore.getUserByName("uhl3"));
        assertNotNull(targetUserStore.getUserByName("uhl4")); // in particular new user uhl4 is expected to be present now
        // the uhl-tenant group is expected to have been merged: the uhl4 user is part of that group in source1
        assertEquals(2, Util.size(targetUserStore.getUserGroupByName("uhl-tenant").getUsers()));
        assertTrue(Util.contains(targetUserStore.getUserGroupByName("uhl-tenant").getUsers(), targetUserStore.getUserByName("uhl")));
        assertTrue(Util.contains(targetUserStore.getUserGroupByName("uhl-tenant").getUsers(), targetUserStore.getUserByName("uhl4")));
    }
}
