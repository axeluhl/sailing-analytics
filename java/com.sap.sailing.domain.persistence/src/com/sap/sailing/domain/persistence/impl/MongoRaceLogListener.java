package com.sap.sailing.domain.persistence.impl;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class MongoRaceLogListener implements RaceLogEventVisitor {

    private final RaceLogIdentifier raceLogIdentifier;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection raceLogsCollection;

    public MongoRaceLogListener(RaceLogIdentifier identifier, MongoObjectFactory mongoObjectFactory, DB database) {
        super();
        this.raceLogIdentifier = identifier;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.raceLogsCollection = this.mongoObjectFactory.getRaceLogCollection();
    }

    private void remember(DBObject eventEntry) {
        raceLogsCollection.insert(eventEntry);
    }

    @Override
    public void visit(RaceLogFlagEvent event) {
        DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(flagEventTrackEntry);
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        DBObject passChangeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(passChangeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        DBObject raceStatusEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(raceStatusEventTrackEntry);
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(startTimeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
        DBObject courseAreaChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(courseAreaChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        DBObject courseDesignChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(courseDesignChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        DBObject finishPositioningListChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(finishPositioningListChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        DBObject finishPositioningConfirmedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(finishPositioningConfirmedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        DBObject pathfinderEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(pathfinderEventTrackEntry);
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        DBObject gateLineOpeningTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(gateLineOpeningTimeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        remember(object);
    }

}
