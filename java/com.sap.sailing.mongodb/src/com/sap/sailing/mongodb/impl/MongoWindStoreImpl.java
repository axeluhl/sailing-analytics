package com.sap.sailing.mongodb.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.mongodb.DomainObjectFactory;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoWindStore;

public class MongoWindStoreImpl implements MongoWindStore {
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
        WindTrack result = domainObjectFactory.loadWindTrack(trackedEvent.getEvent(),
                trackedRace.getRace(), windSource, millisecondsOverWhichToAverage);
        result.addListener(new MongoWindListener(trackedEvent, trackedRace, windSource, mongoObjectFactory, db));
        return result;
    }
}
