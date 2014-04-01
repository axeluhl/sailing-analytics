package com.sap.sailing.domain.tractracadapter.persistence.impl;

import com.mongodb.DB;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory;
import com.sap.sailing.domain.tractracadapter.persistence.PersistenceFactory;

public class PersistenceFactoryImpl implements PersistenceFactory {
    @Override
    public DomainObjectFactory createDomainObjectFactory(DB db, DomainFactory tracTracDomainFactory) {
        return new DomainObjectFactoryImpl(db, tracTracDomainFactory);
    }
}
