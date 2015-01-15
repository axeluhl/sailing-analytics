package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class MongoRaceLogStoreImpl implements RaceLogStore {
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final Map<RaceLogIdentifier, RaceLog> raceLogCache;
    private final WeakHashMap<RaceLog, MongoRaceLogStoreVisitor> listeners;

    public MongoRaceLogStoreImpl(MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.raceLogCache = new HashMap<>();
        this.listeners = new WeakHashMap<RaceLog, MongoRaceLogStoreVisitor>();
    }

    @Override
    public RaceLog getRaceLog(RaceLogIdentifier identifier, boolean ignoreCache) {
        final RaceLog result;
        if (!ignoreCache && raceLogCache.containsKey(identifier)) {
            result = raceLogCache.get(identifier);
        } else {
            result = domainObjectFactory.loadRaceLog(identifier);
            MongoRaceLogStoreVisitor listener = new MongoRaceLogStoreVisitor(identifier, mongoObjectFactory);
            listeners.put(result, listener);
            result.addListener(listener);
            raceLogCache.put(identifier, result);
        }
        return result;
    }

    @Override
    public void removeRaceLog(RaceLogIdentifier identifier) {
        raceLogCache.remove(identifier);
        mongoObjectFactory.removeRaceLog(identifier);
    }

    @Override
    public void removeListenersAddedByStoreFrom(RaceLog raceLog) {
        RaceLogEventVisitor visitor = listeners.get(raceLog);
        if (visitor != null) {
            raceLog.removeListener(visitor);
        }
    }

}
