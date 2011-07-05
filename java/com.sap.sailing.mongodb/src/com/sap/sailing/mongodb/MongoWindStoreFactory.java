package com.sap.sailing.mongodb;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

public interface MongoWindStoreFactory {
    MongoWindStoreFactory INSTANCE = MongoWindStoreFactoryImpl.getDefaultInstance();
    
    String DEFAULT_DB_NAME = "winddb"; 
    
    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
    MongoWindStore getMongoWindStore(MongoObjectFactory mongoObjectFactory) throws UnknownHostException, MongoException;

    /**
     * Obtains a DB instance based on the default settings which can be provided by
     * system properties and/or OSGi bundle properties, respectively. See also
     * {@link #getMongoWindStore(MongoObjectFactory)}.
     */
    DB getDB() throws UnknownHostException;

}