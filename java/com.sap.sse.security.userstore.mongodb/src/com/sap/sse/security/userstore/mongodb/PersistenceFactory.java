package com.sap.sse.security.userstore.mongodb;

import com.sap.sse.security.userstore.mongodb.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    DomainObjectFactory getDefaultDomainObjectFactory();
    MongoObjectFactory getDefaultMongoObjectFactory();
}
