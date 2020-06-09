package com.sap.sailing.shared.persistence.impl;

import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sailing.shared.persistence.DomainObjectFactory;
import com.sap.sailing.shared.persistence.MongoObjectFactory;
import com.sap.sailing.shared.persistence.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    private final DomainObjectFactory defaultDomainObjectFactory;
    private final MongoObjectFactory defaultMongoObjectFactory;
    
    public PersistenceFactoryImpl() {
        this.defaultDomainObjectFactory = new DomainObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
        this.defaultMongoObjectFactory = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
    }

    @Override
    public MongoObjectFactory getDefaultMongoObjectFactory(TypeBasedServiceFinderFactory typeBasedServiceFinderFactory) {
        return new MongoObjectFactoryImpl(defaultMongoObjectFactory.getDatabase(), typeBasedServiceFinderFactory);
    }

    @Override
    public DomainObjectFactory getDefaultDomainObjectFactory(TypeBasedServiceFinderFactory typeBasedServiceFinderFactory) {
        return new DomainObjectFactoryImpl(defaultDomainObjectFactory.getDatabase(), typeBasedServiceFinderFactory);
    }

}
