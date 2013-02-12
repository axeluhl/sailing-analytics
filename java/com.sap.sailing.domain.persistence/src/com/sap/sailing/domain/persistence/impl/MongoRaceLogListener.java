package com.sap.sailing.domain.persistence.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogListener;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class MongoRaceLogListener implements RaceLogListener {
	
	private final RaceColumnIdentifier raceColumnIdentifier;
	private final Fleet fleet;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection raceLogsCollection;
    
    public MongoRaceLogListener(RaceColumnIdentifier identifier, Fleet fleet,
    		MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.raceColumnIdentifier = identifier;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.raceLogsCollection = this.mongoObjectFactory.getRaceLogCollection();
        this.fleet = fleet;
    }

	@Override
	public void flagEventReceived(RaceLogFlagEvent flagEvent) {
		DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceColumnIdentifier, fleet, flagEvent);
		raceLogsCollection.insert(flagEventTrackEntry);
	}

	@Override
	public void startTimeEventReceived(
			RaceLogStartTimeEvent startTimeEvent) {
		DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceColumnIdentifier, fleet, startTimeEvent);
		raceLogsCollection.insert(startTimeEventTrackEntry);

	}

	@Override
	public void eventReceived(RaceLogEvent event) {
		// do nothing, we are interested in the typed events.
	}

}
