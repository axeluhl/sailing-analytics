package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;


import com.mongodb.DB;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

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
    
    @Override
    public Map<Fleet, RaceLog> getRaceLogs(RaceLogIdentifierTemplate template, Iterable<? extends Fleet> fleets) {
    	Map<Fleet, RaceLog> resultMap = new HashMap<Fleet, RaceLog>();
    	
    	for (Fleet fleet : fleets) {
    		RaceLogIdentifier identifier = template.compile(fleet);
    		RaceLog log = domainObjectFactory.loadRaceLog(identifier);
            log.addListener(new MongoRaceLogListener(identifier, mongoObjectFactory, db));
            resultMap.put(fleet, log);
    	}
    	
    	return resultMap;
    }

}
