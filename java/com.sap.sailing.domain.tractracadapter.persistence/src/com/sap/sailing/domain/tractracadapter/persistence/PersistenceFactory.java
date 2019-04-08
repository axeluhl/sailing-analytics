package com.sap.sailing.domain.tractracadapter.persistence;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.persistence.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    DomainObjectFactory createDomainObjectFactory(MongoDatabase db, DomainFactory tracTracDomainFactory);
}
