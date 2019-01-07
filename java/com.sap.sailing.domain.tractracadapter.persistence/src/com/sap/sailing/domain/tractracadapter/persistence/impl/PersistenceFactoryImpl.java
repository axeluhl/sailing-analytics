package com.sap.sailing.domain.tractracadapter.persistence.impl;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.tractracadapter.persistence.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    @Override
    public DomainObjectFactory createDomainObjectFactory(MongoDatabase db, DomainFactory tracTracDomainFactory) {
        return new DomainObjectFactoryImpl(db, tracTracDomainFactory);
    }
}
