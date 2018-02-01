package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoWindStore;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;

public class MongoWindStoreFactoryImpl implements MongoWindStoreFactory {
    private final DB db;
    
    public MongoWindStoreFactoryImpl(DB db) {
        this.db = db;
    }
    
    @Override
    public MongoWindStore getMongoWindStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) throws UnknownHostException, MongoException {
        return new MongoWindStoreImpl(db, mongoObjectFactory, domainObjectFactory);
    }

    public static MongoWindStoreFactory getInstance(DB db) {
        return new MongoWindStoreFactoryImpl(db);
    }

}
