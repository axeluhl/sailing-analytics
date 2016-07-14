package com.sap.sailing.domain.persistence.racelog.tracking;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreFactoryImpl;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

public interface MongoSensorFixStoreFactory {
    MongoSensorFixStoreFactory INSTANCE = new MongoSensorFixStoreFactoryImpl();

    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
    MongoSensorFixStore getMongoGPSFixStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory,
            TypeBasedServiceFinderFactory serviceFinderFactory)
                    throws UnknownHostException, MongoException;

}