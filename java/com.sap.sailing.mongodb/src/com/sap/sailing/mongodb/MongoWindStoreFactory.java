package com.sap.sailing.mongodb;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.sap.sailing.mongodb.impl.Activator;
import com.sap.sailing.mongodb.impl.MongoWindStoreFactoryImpl;

public interface MongoWindStoreFactory {
    MongoWindStoreFactory INSTANCE = new MongoWindStoreFactoryImpl(Activator.getDefaultInstance().getDB());
    
    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
    MongoWindStore getMongoWindStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) throws UnknownHostException, MongoException;

}