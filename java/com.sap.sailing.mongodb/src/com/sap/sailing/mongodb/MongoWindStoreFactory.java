package com.sap.sailing.mongodb;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

public interface MongoWindStoreFactory {
    MongoWindStoreFactory INSTANCE = MongoWindStoreFactoryImpl.getDefaultInstance();
    
    String DEFAULT_DB_NAME = "winddb"; 
    
    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     * @param mongoObjectFactory TODO
     */
    MongoWindStore getMongoWindStore(MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException;

    MongoWindStore getMongoWindStore(String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException;
    
    MongoWindStore getMongoWindStore(int port, String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException;

    MongoWindStore getMongoWindStore(String hostname, int port, String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException;
}
