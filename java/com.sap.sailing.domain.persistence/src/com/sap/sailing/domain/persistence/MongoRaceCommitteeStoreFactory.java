package com.sap.sailing.domain.persistence;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.impl.MongoRaceCommitteeStoreFactoryImpl;
import com.sap.sailing.mongodb.MongoDBService;

public interface MongoRaceCommitteeStoreFactory {
	MongoRaceCommitteeStoreFactory INSTANCE = new MongoRaceCommitteeStoreFactoryImpl(MongoDBService.INSTANCE.getDB());
	
	/**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
	MongoRaceCommitteeStore getMongoRaceCommitteeStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory)
            throws UnknownHostException, MongoException;
}
