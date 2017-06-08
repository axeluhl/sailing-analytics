package com.sap.sailing.domain.persistence.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MongoWindListener implements com.sap.sailing.domain.tracking.WindListener {
    private final TrackedRace trackedRace;
    private final String regattaName;
    private final WindSource windSource;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection windTracksCollection;

    public MongoWindListener(TrackedRace trackedRace, String regattaName, WindSource windSource,
            MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.regattaName = regattaName;
        this.trackedRace = trackedRace;
        this.windSource = windSource;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.windTracksCollection = this.mongoObjectFactory.getWindTrackCollection();
    }

    @Override
    public void windDataReceived(Wind wind) {
        DBObject windTrackEntry = mongoObjectFactory.storeWindTrackEntry(trackedRace.getRace(), regattaName, windSource, wind);
        windTracksCollection.insert(windTrackEntry);
    }

    @Override
    public void windDataRemoved(Wind wind) {
        DBObject windTrackEntry = mongoObjectFactory.storeWindTrackEntry(trackedRace.getRace(), regattaName, windSource, wind);
        windTracksCollection.remove(windTrackEntry);
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        // no need to change anything in MongoDB because MongoDB only keeps track of the fixes, not their averaging
    }

}
