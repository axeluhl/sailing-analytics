package com.sap.sailing.domain.persistence.impl;


import com.mongodb.DB;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class MongoRaceLogStoreFactoryImpl implements MongoRaceLogStoreFactory {
    private final DB db;
    
    public MongoRaceLogStoreFactoryImpl(DB db) {
        this.db = db;
    }

    @Override
    public RaceLogStore getMongoRaceLogStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        return new MongoRaceLogStoreImpl(db, mongoObjectFactory, domainObjectFactory);
    }

}
