package com.sap.sse.replication.persistence;

import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.persistence.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();

    DomainObjectFactory getDomainObjectFactory(MongoDBService mongoDbService);

    MongoObjectFactory getMongoObjectFactory(MongoDBService mongoDbService);

    DomainObjectFactory getDefaultDomainObjectFactory();

    MongoObjectFactory getDefaultMongoObjectFactory();

}
