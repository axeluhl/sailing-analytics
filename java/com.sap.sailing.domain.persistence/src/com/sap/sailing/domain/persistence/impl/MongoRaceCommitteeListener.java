package com.sap.sailing.domain.persistence.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeListener;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class MongoRaceCommitteeListener implements RaceCommitteeListener {
	
	private final TrackedRegatta trackedRegatta;
    private final TrackedRace trackedRace;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection raceCommitteeEventTracksCollection;
    
    public MongoRaceCommitteeListener(TrackedRegatta trackedRegatta, TrackedRace trackedRace,
    		MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.trackedRegatta = trackedRegatta;
        this.trackedRace = trackedRace;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.raceCommitteeEventTracksCollection = this.mongoObjectFactory.getRaceCommitteeTrackCollection();
    }

	@Override
	public void flagEventReceived(RaceCommitteeFlagEvent flagEvent) {
		DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceCommitteeTrackEntry(trackedRegatta.getRegatta(), 
				trackedRace.getRace(), flagEvent);
		raceCommitteeEventTracksCollection.insert(flagEventTrackEntry);
	}

	@Override
	public void startTimeEventReceived(
			RaceCommitteeStartTimeEvent startTimeEvent) {
		DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceCommitteeTrackEntry(trackedRegatta.getRegatta(), 
				trackedRace.getRace(), startTimeEvent);
		raceCommitteeEventTracksCollection.insert(startTimeEventTrackEntry);

	}

}
