package com.sap.sse.security.userstore.mongodb;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.userstore.mongodb.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    static PersistenceFactory create(MongoDBService mongoDBService) {
        return new PersistenceFactoryImpl(mongoDBService);
    }
    
    DomainObjectFactory getDefaultDomainObjectFactory();
    MongoObjectFactory getDefaultMongoObjectFactory();
}
