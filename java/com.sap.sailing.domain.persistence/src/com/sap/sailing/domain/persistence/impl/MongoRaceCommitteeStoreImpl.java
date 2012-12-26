package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceCommitteeStore;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.impl.EmptyRaceCommitteeStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class MongoRaceCommitteeStoreImpl extends EmptyRaceCommitteeStore implements MongoRaceCommitteeStore {
	private transient final DB db;
	private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    
    public MongoRaceCommitteeStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory)
            throws UnknownHostException, MongoException {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
    }

	@Override
	public RaceCommitteeEventTrack getRaceCommitteeEventTrack(TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
		RaceCommitteeEventTrack track;
		track = domainObjectFactory.loadRaceCommitteeEventTrack(trackedRegatta.getRegatta(), trackedRace.getRace());
		track.addListener(new MongoRaceCommitteeListener(trackedRegatta, trackedRace, mongoObjectFactory, db));
		return track;
	}

}
