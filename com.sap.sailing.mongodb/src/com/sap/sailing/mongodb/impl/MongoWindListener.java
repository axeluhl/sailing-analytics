package com.sap.sailing.mongodb.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.mongodb.MongoObjectFactory;

public class MongoWindListener implements com.sap.sailing.domain.tracking.WindListener {
    private final String eventName;
    private final String raceName;
    private final String windSourceName;
    private final MongoObjectFactory mongoObjectFactory;
    private final DBCollection windTracksCollection;

    public MongoWindListener(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource,
            MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.eventName = trackedEvent.getEvent().getName();
        this.raceName = trackedRace.getRace().getName();
        this.windSourceName = windSource.name();
        this.mongoObjectFactory = mongoObjectFactory;
        this.windTracksCollection = mongoObjectFactory.getWindTrackCollection(database);
    }

    @Override
    public void windDataReceived(Wind wind) {
        DBObject windForDB = mongoObjectFactory.storeWind(wind);
    }

}
