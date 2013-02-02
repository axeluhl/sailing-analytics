package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStore;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class MongoRaceLogStoreImpl extends EmptyRaceLogStore implements MongoRaceLogStore {
	private transient final DB db;
	private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    
    public MongoRaceLogStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory)
            throws UnknownHostException, MongoException {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
    }

	@Override
	public RaceLog getRaceLog(TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
		RaceLog track;
		track = domainObjectFactory.loadRaceLog(trackedRegatta.getRegatta(), trackedRace.getRace());
		track.addListener(new MongoRaceLogListener(trackedRegatta, trackedRace, mongoObjectFactory, db));
		return track;
	}

}
