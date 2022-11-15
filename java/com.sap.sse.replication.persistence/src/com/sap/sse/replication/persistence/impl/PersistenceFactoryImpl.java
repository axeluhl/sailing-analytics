package com.sap.sse.replication.persistence.impl;

import com.sap.sse.replication.persistence.PersistenceFactory;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.persistence.DomainObjectFactory;
import com.sap.sse.replication.persistence.MongoObjectFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    @Override
    public DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDbService) {
        return new DomainObjectFactoryImpl(mongoDbService.getDB());
    }

    @Override
    public MongoObjectFactory getMongoObjectFactory(MongoDBService mongoDbService) {
        return new MongoObjectFactoryImpl(mongoDbService.getDB());
    }

    @Override
    public DomainObjectFactory getDefaultDomainObjectFactory() {
        return getDomainObjectFactory(MongoDBConfiguration.getDefaultConfiguration().getService());
    }

    @Override
    public MongoObjectFactory getDefaultMongoObjectFactory() {
        return getMongoObjectFactory(MongoDBConfiguration.getDefaultConfiguration().getService());
    }
}
