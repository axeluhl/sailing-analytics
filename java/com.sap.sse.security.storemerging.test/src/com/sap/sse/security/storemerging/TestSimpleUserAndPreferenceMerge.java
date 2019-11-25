package com.sap.sse.security.storemerging;

import java.io.IOException;
import java.util.UUID;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
    public void testImportFromSource1ToTarget1() throws UserGroupManagementException, UserManagementException {
        final SecurityStoreMerger merger = new SecurityStoreMerger(cfgForTarget, defaultCreationGroupNameForTarget);
        final Pair<UserStore, AccessControlStore> sourceStores = merger.importStores(cfgForSource, defaultCreationGroupNameForSource);
        final UserStore targetUserStore = merger.getTargetUserStore();
        final AccessControlStore targetAccessControlStore = merger.getTargetAccessControlStore();
    }
}
