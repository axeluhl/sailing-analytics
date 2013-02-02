package com.sap.sailing.domain.persistence.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogListener;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

public class MongoRaceLogListener implements RaceLogListener {
	
	private final TrackedRegatta trackedRegatta;
    private final TrackedRace trackedRace;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection raceLogsCollection;
    
    public MongoRaceLogListener(TrackedRegatta trackedRegatta, TrackedRace trackedRace,
    		MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.trackedRegatta = trackedRegatta;
        this.trackedRace = trackedRace;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.raceLogsCollection = this.mongoObjectFactory.getRaceLogCollection();
    }

	@Override
	public void flagEventReceived(RaceLogFlagEvent flagEvent) {
		DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(trackedRegatta.getRegatta(), 
				trackedRace.getRace(), flagEvent);
		raceLogsCollection.insert(flagEventTrackEntry);
	}

	@Override
	public void startTimeEventReceived(
			RaceLogStartTimeEvent startTimeEvent) {
		DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(trackedRegatta.getRegatta(), 
				trackedRace.getRace(), startTimeEvent);
		raceLogsCollection.insert(startTimeEventTrackEntry);

	}

}
