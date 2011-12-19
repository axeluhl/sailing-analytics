package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.Before;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.mongodb.MongoDBConfiguration;

public abstract class AbstractMongoDBTest {
    protected Mongo mongo;
    protected DB db;
    private final MongoDBConfiguration dbConfiguration;
    
    public AbstractMongoDBTest() {
        dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
    }
    
    protected MongoDBConfiguration getDBConfiguration() {
        return dbConfiguration;
    }
    
    protected Mongo newMongo() throws UnknownHostException, MongoException {
        return new Mongo(System.getProperty("mongo.host", "127.0.0.1"), dbConfiguration.getPort());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException {
        mongo = newMongo();
        assertNotNull(mongo);
        mongo.dropDatabase(getDBConfiguration().getDatabaseName());
        db = mongo.getDB(getDBConfiguration().getDatabaseName());
        assertNotNull(db);
    }
}
