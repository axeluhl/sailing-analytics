package com.sap.sailing.domain.persistence.racelog.tracking;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
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

    /**
     * Like {@link #getMongoGPSFixStore(MongoObjectFactory, DomainObjectFactory, TypeBasedServiceFinderFactory)}, but
     * additionally the write concern for the MongoDB writes can be controlled which otherwise usually defaults
     * to {@link WriteConcern#UNACKNOWLEDGED} for the fixes as well as the metadata.
     */
    MongoSensorFixStore getMongoGPSFixStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory,
            TypeBasedServiceFinderFactory serviceFinderFactory, WriteConcern writeConcern)
                    throws UnknownHostException, MongoException;

}