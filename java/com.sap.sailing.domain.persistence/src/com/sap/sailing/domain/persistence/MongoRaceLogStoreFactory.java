package com.sap.sailing.domain.persistence;

import com.sap.sailing.domain.persistence.impl.MongoRaceLogStoreFactoryImpl;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.mongodb.MongoDBService;

public interface MongoRaceLogStoreFactory {
	MongoRaceLogStoreFactory INSTANCE = new MongoRaceLogStoreFactoryImpl(MongoDBService.INSTANCE.getDB());
	
	/**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
	MongoRaceLogStore getMongoRaceLogStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory,
			RaceColumnIdentifier identifier);
}
