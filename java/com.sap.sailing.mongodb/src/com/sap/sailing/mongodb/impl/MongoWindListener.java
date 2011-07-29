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
    private final TrackedEvent trackedEvent;
    private final TrackedRace trackedRace;
    private final WindSource windSource;
    private final MongoObjectFactory mongoObjectFactory;
    private final DBCollection windTracksCollection;

    public MongoWindListener(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource,
            MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.trackedEvent = trackedEvent;
        this.trackedRace = trackedRace;
        this.windSource = windSource;
        this.mongoObjectFactory = mongoObjectFactory;
        this.windTracksCollection = mongoObjectFactory.getWindTrackCollection();
    }

    @Override
    public void windDataReceived(Wind wind) {
        DBObject windTrackEntry = mongoObjectFactory.storeWindTrackEntry(trackedEvent.getEvent(), trackedRace.getRace(), windSource, wind);
        windTracksCollection.insert(windTrackEntry);
    }

    @Override
    public void windDataRemoved(Wind wind) {
        DBObject windTrackEntry = mongoObjectFactory.storeWindTrackEntry(trackedEvent.getEvent(), trackedRace.getRace(), windSource, wind);
        windTracksCollection.remove(windTrackEntry);
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        // no need to change anything in MongoDB because MongoDB only keeps track of the fixes, not their averaging
    }

}
