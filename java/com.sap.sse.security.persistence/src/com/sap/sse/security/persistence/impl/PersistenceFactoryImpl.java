package com.sap.sse.security.persistence.impl;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.persistence.DomainObjectFactory;
import com.sap.sse.security.persistence.MongoObjectFactory;
import com.sap.sse.security.persistence.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    private final DomainObjectFactory defaultDomainObjectFactory;
    private final MongoObjectFactory defaultMongoObjectFactory;
    
    public PersistenceFactoryImpl() {
        this.defaultDomainObjectFactory = new DomainObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
        this.defaultMongoObjectFactory = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
    }

    @Override
    public MongoObjectFactory getDefaultMongoObjectFactory() {
        return defaultMongoObjectFactory;
    }

    @Override
    public DomainObjectFactory getDefaultDomainObjectFactory() {
        return defaultDomainObjectFactory;
    }

}
