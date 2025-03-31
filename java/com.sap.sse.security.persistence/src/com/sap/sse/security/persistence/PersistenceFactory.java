package com.sap.sse.security.persistence;

import com.sap.sse.security.persistence.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    MongoObjectFactory getDefaultMongoObjectFactory();
    DomainObjectFactory getDefaultDomainObjectFactory();
}
