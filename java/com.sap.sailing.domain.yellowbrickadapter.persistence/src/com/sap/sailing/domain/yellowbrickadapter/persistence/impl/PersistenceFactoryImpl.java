package com.sap.sailing.domain.yellowbrickadapter.persistence.impl;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.yellowbrickadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.yellowbrickadapter.persistence.MongoObjectFactory;
import com.sap.sailing.domain.yellowbrickadapter.persistence.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    @Override
    public DomainObjectFactory createDomainObjectFactory(MongoDatabase db) {
        return new DomainObjectFactoryImpl(db);
    }
    
    @Override
    public MongoObjectFactory createMongoObjectFactory(MongoDatabase db) {
        return new MongoObjectFactoryImpl(db);
    }
}
