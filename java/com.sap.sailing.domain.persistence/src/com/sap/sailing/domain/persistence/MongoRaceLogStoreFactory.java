package com.sap.sailing.domain.persistence;


import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.persistence.impl.MongoRaceLogStoreFactoryImpl;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;

public interface MongoRaceLogStoreFactory {
    MongoRaceLogStoreFactory INSTANCE = new MongoRaceLogStoreFactoryImpl();

    /**
     * Gets the default Mongo store based on the properties mongo.hostname[=localhost], mongo.port[=27017]
     * and mongo.dbName[=&lt;contents of constant {@link #DEFAULT_DB_NAME}&gt;] specified in the bundle context
     */
    RaceLogStore getMongoRaceLogStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory);
    
    RaceLogEventVisitor getMongoRaceLogStoreVisitor(RaceLogIdentifier identifier, MongoObjectFactory mongoObjectFactory);
}
