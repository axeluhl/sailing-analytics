package com.sap.sailing.mongodb.impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.mongodb.MongoObjectFactory;
import com.sap.sailing.mongodb.MongoWindStore;

public class MongoWindStoreImpl implements MongoWindStore {
    private final Mongo mongo;
    private final DB db;
    private final MongoObjectFactory mongoObjectFactory;

    /**
     * Connects to the default instance of MongoDB on localhost
     */
    public MongoWindStoreImpl(String dbName, MongoObjectFactory mongoObjectFactory) throws UnknownHostException,
            MongoException {
        mongo = new Mongo();
        db = mongo.getDB(dbName);
        this.mongoObjectFactory = mongoObjectFactory;
    }

    public MongoWindStoreImpl(int port, String dbName, MongoObjectFactory mongoObjectFactory)
            throws UnknownHostException, MongoException {
        this("127.0.0.1", port, dbName, mongoObjectFactory);
    }

    public MongoWindStoreImpl(String hostname, int port, String dbName, MongoObjectFactory mongoObjectFactory)
            throws UnknownHostException, MongoException {
        mongo = new Mongo(hostname, port);
        db = mongo.getDB(dbName);
        this.mongoObjectFactory = mongoObjectFactory;
    }

    /**
     * Loads the wind track from the database and adds a {@link MongoWindListener} listener such that
     * additions to the wind track will be written to the MongoDB.
     */
    @Override
    public WindTrack getWindTrack(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource,
            long millisecondsOverWhichToAverage) {
        WindTrack result = DomainObjectFactoryImpl.INSTANCE.loadWindTrack(trackedEvent.getEvent(),
                trackedRace.getRace(), windSource, millisecondsOverWhichToAverage, db);
        result.addListener(new MongoWindListener(trackedEvent, trackedRace, windSource, mongoObjectFactory, db));
        return result;
    }
}
