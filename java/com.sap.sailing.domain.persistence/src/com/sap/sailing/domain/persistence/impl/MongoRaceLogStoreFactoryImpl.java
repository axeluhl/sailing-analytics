package com.sap.sailing.domain.persistence.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class MongoRaceLogStoreFactoryImpl implements MongoRaceLogStoreFactory {
    @Override
    public RaceLogStore getMongoRaceLogStore(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        return new MongoRaceLogStoreImpl(mongoObjectFactory, domainObjectFactory);
    }

    @Override
    public RaceLogEventVisitor getMongoRaceLogStoreVisitor(RaceLogIdentifier identifier, MongoObjectFactory mongoObjectFactory) {
        return new MongoRaceLogStoreVisitor(identifier, mongoObjectFactory);
    }
}
