package com.sap.sailing.domain.persistence.impl;

import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoDBService;

public class MongoFactoryImpl implements MongoFactory {
    private final DomainObjectFactory defaultDomainObjectFactory;
    private final MongoObjectFactory defaultMongoObjectFactory;
    
    public MongoFactoryImpl() {
        super();
        this.defaultDomainObjectFactory = new DomainObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
        this.defaultMongoObjectFactory = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
    }

    @Override
    public DomainObjectFactory getDefaultDomainObjectFactory() {
        return defaultDomainObjectFactory;
    }

    @Override
    public DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDBService) {
        return new DomainObjectFactoryImpl(mongoDBService.getDB());
    }

    @Override
    public MongoObjectFactory getDefaultMongoObjectFactory() {
        return defaultMongoObjectFactory;
    }

    @Override
    public MongoObjectFactory getMongoObjectFactory(MongoDBService mongoDBService) {
        return new MongoObjectFactoryImpl(mongoDBService.getDB());
    }

}
