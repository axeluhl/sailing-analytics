package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertNotNull;

import java.net.UnknownHostException;

import org.junit.Before;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

public abstract class AbstractMongoDBTest implements MongoDBTest {
    protected Mongo mongo;
    protected DB db;
    
    protected Mongo newMongo() throws UnknownHostException, MongoException {
        return new Mongo(System.getProperty("mongo.host", "127.0.0.1"),
                ((MongoWindStoreFactoryImpl) MongoWindStoreFactoryImpl.getDefaultInstance()).getDefaultPort());
    }
    
    @Before
    public void dropTestDB() throws UnknownHostException, MongoException {
        mongo = newMongo();
        assertNotNull(mongo);
        mongo.dropDatabase(WIND_TEST_DB);
        db = mongo.getDB(WIND_TEST_DB);
        assertNotNull(db);
    }
}
