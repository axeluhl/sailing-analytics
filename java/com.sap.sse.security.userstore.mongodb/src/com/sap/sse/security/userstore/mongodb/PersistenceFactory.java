package com.sap.sse.security.userstore.mongodb;

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.userstore.mongodb.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    static PersistenceFactory create(MongoDBService mongoDBService) {
        return new PersistenceFactoryImpl(mongoDBService);
    }
    
    DomainObjectFactory getDefaultDomainObjectFactory();
    MongoObjectFactory getDefaultMongoObjectFactory();

    DomainObjectFactory getDomainObjectFactory(ReadConcern readConcern, WriteConcern writeConcern);

    MongoObjectFactory getMongoObjectFactory(ReadConcern readConcern, WriteConcern writeConcern);
}
