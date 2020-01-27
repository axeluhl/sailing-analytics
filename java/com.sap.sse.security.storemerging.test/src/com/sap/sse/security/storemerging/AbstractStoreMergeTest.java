package com.sap.sse.security.storemerging;

import java.io.IOException;
import java.util.UUID;

import org.bson.Document;
import org.junit.After;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.security.storemerging.test.MongoDBFiller;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

public abstract class AbstractStoreMergeTest {
    private final static String importSourceMongoDbUri = "mongodb://localhost/"+UUID.randomUUID();
    protected final static String defaultCreationGroupNameForSource = "dummy-default-creation-group-for-source";
    protected final static String defaultCreationGroupNameForTarget = "dummy-default-creation-group-for-target";
    protected MongoDBConfiguration cfgForSource;
    private MongoDatabase targetDb;
    protected MongoDBConfiguration cfgForTarget;

    protected void setUp(String sourceVariant, String targetVariant) throws IOException {
        cfgForTarget = MongoDBConfiguration.getDefaultTestConfiguration();
        targetDb = cfgForTarget.getService().getDB();
        fill(targetVariant, targetDb);
        cfgForSource = new MongoDBConfiguration(new MongoClientURI(importSourceMongoDbUri));
        fill(sourceVariant, cfgForSource.getService().getDB());
    }
    
    @After
    public void tearDown() {
        cfgForSource.getService().getDB().drop();
    }

    private void fill(final String variant, final MongoDatabase db) throws IOException {
        for (final CollectionNames collectionName : new CollectionNames[] { CollectionNames.USERS,
                CollectionNames.USER_GROUPS, CollectionNames.OWNERSHIPS, CollectionNames.ACCESS_CONTROL_LISTS,
                CollectionNames.PREFERENCES, CollectionNames.ROLES }) {
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
}
