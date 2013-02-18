package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStore;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;

public class MongoRaceLogStoreImpl extends EmptyRaceLogStore implements MongoRaceLogStore {
	private transient final DB db;
	private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final Map<RaceLogIdentifier, RaceLog> raceLogCache;
    
    public MongoRaceLogStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory) {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.raceLogCache = new HashMap<RaceLogIdentifier, RaceLog>();
    }

	@Override
	public RaceLog getRaceLog(RaceLogIdentifier identifier) {
		RaceLog log;
		if (raceLogCache.containsKey(identifier)) {
			log = raceLogCache.get(identifier);
		} else {
			log = domainObjectFactory.loadRaceLog(identifier);
			log.addListener(new MongoRaceLogListener(identifier, mongoObjectFactory, db));
			raceLogCache.put(identifier, log);
		}
		
		return log;
	}

}
