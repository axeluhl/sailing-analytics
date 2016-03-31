package com.sap.sailing.domain.persistence.racelog.tracking;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoGPSFixStoreFactoryImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;

public interface MongoGPSFixStoreFactory {
    MongoGPSFixStoreFactory INSTANCE = new MongoGPSFixStoreFactoryImpl();

    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
    MongoGPSFixStore getMongoGPSFixStore(SensorFixStore sensorFixStore) throws UnknownHostException, MongoException;

}