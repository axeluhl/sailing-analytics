package com.sap.sailing.shared.persistence;

import com.sap.sailing.shared.persistence.impl.PersistenceFactoryImpl;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    MongoObjectFactory getDefaultMongoObjectFactory(TypeBasedServiceFinderFactory typeBasedServiceFinderFactory);
    DomainObjectFactory getDefaultDomainObjectFactory(TypeBasedServiceFinderFactory typeBasedServiceFinderFactory);
}
