package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;

public abstract class AbstractMongoDBTest {
    protected MongoClient mongo;
    protected MongoDatabase db;
    private final MongoDBConfiguration dbConfiguration;
    private MongoDBService mongoDBService;
    
    public AbstractMongoDBTest() throws UnknownHostException, MongoException {
        dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        mongoDBService = getDBConfiguration().getService();
        mongo = newMongo();
        db = mongoDBService.getDB();
    }
    
    protected MongoDBConfiguration getDBConfiguration() {
        return dbConfiguration;
    }
    
    protected MongoClient newMongo() throws UnknownHostException, MongoException {
        return MongoClients.create(dbConfiguration.getMongoClientURI());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException, InterruptedException {
        assertNotNull(mongo);
        dropAllCollections(db);
        assertNotNull(db);
    }
    
    @After
    public void tearDown() {
    }

    private void dropAllCollections(MongoDatabase theDB) throws InterruptedException {
        db.drop();
    }

    protected MongoDBService getMongoService() {
        return mongoDBService;
    }
}
