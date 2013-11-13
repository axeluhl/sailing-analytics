package com.sap.sailing.domain.tractracadapter.persistence;

import com.mongodb.DB;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.persistence.impl.PersistenceFactoryImpl;

public interface PersistenceFactory {
    PersistenceFactory INSTANCE = new PersistenceFactoryImpl();
    
    DomainObjectFactory createDomainObjectFactory(DB db, DomainFactory tracTracDomainFactory);
}
