package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.Before;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.mongodb.MongoDBConfiguration;
import com.sap.sailing.mongodb.MongoDBService;

public abstract class AbstractMongoDBTest {
    protected Mongo mongo;
    protected DB db;
    private final MongoDBConfiguration dbConfiguration;
    private MongoDBService service;
    
    public AbstractMongoDBTest() {
        dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        service = MongoDBService.INSTANCE;
        service.setConfiguration(getDBConfiguration());
    }
    
    protected MongoDBConfiguration getDBConfiguration() {
        return dbConfiguration;
    }
    
    protected Mongo newMongo() throws UnknownHostException, MongoException {
        return new Mongo(System.getProperty("mongo.host", "127.0.0.1"), dbConfiguration.getPort());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException, InterruptedException {
        mongo = newMongo();
        assertNotNull(mongo);
        db = mongo.getDB(getDBConfiguration().getDatabaseName());
        dropAllCollections(db);
        assertNotNull(db);
    }

    private void dropAllCollections(DB theDB) throws InterruptedException {
        db.dropDatabase();
    }

    protected MongoDBService getMongoService() {
        return service;
    }
}
