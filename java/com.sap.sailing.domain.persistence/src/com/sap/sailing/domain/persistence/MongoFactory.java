package com.sap.sailing.domain.persistence;

import com.sap.sailing.domain.persistence.impl.MongoFactoryImpl;
import com.sap.sailing.mongodb.MongoDBService;

public interface MongoFactory {
    MongoFactory INSTANCE = new MongoFactoryImpl();
    
    DomainObjectFactory getDefaultDomainObjectFactory();
    DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDBService);
    MongoObjectFactory getDefaultMongoObjectFactory();
    MongoObjectFactory getMongoObjectFactory(MongoDBService mongoDBService);
}
