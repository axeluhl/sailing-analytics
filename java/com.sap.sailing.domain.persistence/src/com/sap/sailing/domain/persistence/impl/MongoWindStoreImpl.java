package com.sap.sailing.domain.persistence.impl;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoWindStore;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;

/**
 * Stores wind tracks of sources that {@link WindSource#canBeStored() can be stored}. The {@link EmptyWindStore}'s
 * factory method is used for wind tracks for other sources.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MongoWindStoreImpl extends EmptyWindStore implements MongoWindStore {
    private final DB db;
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;

    public MongoWindStoreImpl(DB db, MongoObjectFactory mongoObjectFactory, DomainObjectFactory domainObjectFactory)
            throws UnknownHostException, MongoException {
        this.db = db;
        this.mongoObjectFactory = mongoObjectFactory;
        this.domainObjectFactory = domainObjectFactory;
    }

    /**
     * Loads the wind track from the database and adds a {@link MongoWindListener} listener such that
     * additions to the wind track will be written to the MongoDB.
     */
    @Override
    public WindTrack getWindTrack(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource,
            long millisecondsOverWhichToAverage) {
        WindTrack result;
        if (windSource.canBeStored()) {
            result = domainObjectFactory.loadWindTrack(trackedEvent.getEvent(), trackedRace.getRace(), windSource,
                    millisecondsOverWhichToAverage);
            result.addListener(new MongoWindListener(trackedEvent, trackedRace, windSource, mongoObjectFactory, db));
        } else {
            result = super.getWindTrack(trackedEvent, trackedRace, windSource, millisecondsOverWhichToAverage);
        }
        return result;
    }

    @Override
    public Map<? extends WindSource, ? extends WindTrack> loadWindTracks(TrackedEvent trackedEvent,
            TrackedRace trackedRace, long millisecondsOverWhichToAverageWind) {
        Map<? extends WindSource, ? extends WindTrack> result = domainObjectFactory.loadWindTracks(
                trackedEvent.getEvent(), trackedRace.getRace(), millisecondsOverWhichToAverageWind);
        for (Entry<? extends WindSource, ? extends WindTrack> e : result.entrySet()) {
            e.getValue().addListener(new MongoWindListener(trackedEvent, trackedRace, e.getKey(), mongoObjectFactory, db));
        }
        return result;
    }
}
