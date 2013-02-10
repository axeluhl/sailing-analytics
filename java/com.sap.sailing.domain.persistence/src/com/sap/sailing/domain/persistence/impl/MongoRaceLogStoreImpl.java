package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStore;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;

public class MongoRaceLogStoreImpl extends EmptyRaceLogStore implements MongoRaceLogStore {
	private transient final DB db;
	private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final RaceColumnIdentifier identifier;
    private final Map<Fleet, RaceLog> raceLogCache;
    
    public MongoRaceLogStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory,
    		RaceColumnIdentifier identifier) {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
        this.identifier = identifier;
        this.raceLogCache = new HashMap<Fleet, RaceLog>();
        
    }

	@Override
	public RaceLog getRaceLog(Fleet fleet) {
		RaceLog log;
		if (!raceLogCache.containsKey(fleet)) {
			log = domainObjectFactory.loadRaceLog(identifier, fleet);
			log.addListener(new MongoRaceLogListener(identifier, fleet, mongoObjectFactory, db));
			raceLogCache.put(fleet, log);
		} else {
			log = raceLogCache.get(fleet);
		}
		return log;
	}

}
