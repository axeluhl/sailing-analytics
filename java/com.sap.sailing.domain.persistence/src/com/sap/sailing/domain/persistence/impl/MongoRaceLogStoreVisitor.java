package com.sap.sailing.domain.persistence.impl;

import java.util.logging.Logger;

import com.mongodb.DBObject;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;

public class MongoRaceLogStoreVisitor implements RaceLogEventVisitor {

    private final RaceLogIdentifier raceLogIdentifier;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    
    final static Logger logger = Logger.getLogger(MongoRaceLogStoreVisitor.class.getName());

    public MongoRaceLogStoreVisitor(RaceLogIdentifier identifier, MongoObjectFactory mongoObjectFactory) {
        super();
        this.raceLogIdentifier = identifier;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
    }

    @Override
    public void visit(RaceLogFlagEvent event) {
        DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(flagEventTrackEntry);
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        DBObject passChangeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(passChangeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        DBObject raceStatusEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(raceStatusEventTrackEntry);
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(startTimeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
        DBObject courseAreaChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(courseAreaChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        DBObject courseDesignChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(courseDesignChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        DBObject finishPositioningListChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(finishPositioningListChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogDeviceCompetitorMappingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogDeviceMarkMappingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogDenoteForTrackingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogStartTrackingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogRevokeEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogRegisterCompetitorEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogDefineMarkEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }
    
    @Override
    public void visit(RaceLogCloseOpenEndedDeviceMappingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogAdditionalScoringInformationEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }
}
