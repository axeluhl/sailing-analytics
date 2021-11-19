package com.sap.sailing.domain.yellowbrickadapter.persistence;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.yellowbrickadapter.persistence.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    DomainObjectFactory createDomainObjectFactory(MongoDatabase db);
    
    MongoObjectFactory createMongoObjectFactory(MongoDatabase db);
}
