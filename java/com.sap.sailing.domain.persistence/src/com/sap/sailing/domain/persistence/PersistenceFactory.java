package com.sap.sailing.domain.persistence;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.persistence.impl.PersistenceFactoryImpl;
import com.sap.sailing.mongodb.MongoDBService;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    DomainObjectFactory getDefaultDomainObjectFactory();
    DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDBService, DomainFactory baseDomainFactory);
    MongoObjectFactory getDefaultMongoObjectFactory();
    MongoObjectFactory getMongoObjectFactory(MongoDBService mongoDBService);
}
