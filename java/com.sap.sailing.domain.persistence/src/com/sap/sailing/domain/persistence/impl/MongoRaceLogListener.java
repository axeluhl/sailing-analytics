package com.sap.sailing.domain.persistence.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogListener;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class MongoRaceLogListener implements RaceLogListener {
	
	private final RaceColumnIdentifier raceColumnIdentifier;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection raceLogsCollection;
    
    public MongoRaceLogListener(RaceColumnIdentifier identifier,
    		MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.raceColumnIdentifier = identifier;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.raceLogsCollection = this.mongoObjectFactory.getRaceLogCollection();
    }

	@Override
	public void flagEventReceived(RaceLogFlagEvent flagEvent) {
		DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceColumnIdentifier, flagEvent);
		raceLogsCollection.insert(flagEventTrackEntry);
	}

	@Override
	public void startTimeEventReceived(
			RaceLogStartTimeEvent startTimeEvent) {
		DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceColumnIdentifier, startTimeEvent);
		raceLogsCollection.insert(startTimeEventTrackEntry);

	}

}
