package com.sap.sailing.domain.persistence.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalog.RegattaLogStore;

public class MongoRegattaLogStoreFactoryImpl implements MongoRegattaLogStoreFactory {
    @Override
    public RegattaLogStore getMongoRegattaLogStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        return new MongoRegattaLogStoreImpl(mongoObjectFactory, domainObjectFactory);
    }

    @Override
    public RegattaLogEventVisitor getMongoRegattaLogStoreVisitor(RegattaLikeIdentifier identifier, MongoObjectFactory mongoObjectFactory) {
        return new MongoRegattaLogStoreVisitor(identifier, mongoObjectFactory);
    }
}
