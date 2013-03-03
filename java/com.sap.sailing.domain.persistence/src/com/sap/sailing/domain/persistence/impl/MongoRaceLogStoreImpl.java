package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class MongoRaceLogStoreImpl implements RaceLogStore {
    private transient final DB db;
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final Map<RaceLogIdentifier, RaceLog> raceLogCache;

    public MongoRaceLogStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.raceLogCache = new HashMap<>();
    }

    @Override
    public RaceLog getRaceLog(RaceLogIdentifier identifier) {
        if (raceLogCache.containsKey(identifier)) {
            return raceLogCache.get(identifier);
        }
        RaceLog result = domainObjectFactory.loadRaceLog(identifier);
        result.addListener(new MongoRaceLogListener(identifier, mongoObjectFactory, db));
        raceLogCache.put(identifier, result);
        return result;
    }

}
