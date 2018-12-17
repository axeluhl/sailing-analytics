package com.sap.sailing.domain.persistence.impl;

import java.util.logging.Logger;

import org.bson.Document;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
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
        Document flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(flagEventTrackEntry);
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        Document passChangeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(passChangeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        Document raceStatusEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(raceStatusEventTrackEntry);
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        Document startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(startTimeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        Document courseDesignChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(courseDesignChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        Document finishPositioningListChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(finishPositioningListChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogDenoteForTrackingEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogStartTrackingEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogRevokeEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogRegisterCompetitorEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogAdditionalScoringInformationEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }
    
        @Override
    public void visit(RaceLogFixedMarkPassingEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogSuppressedMarkPassingsEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogDependentStartTimeEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogStartOfTrackingEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogEndOfTrackingEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }

    @Override
    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }
    
    @Override
    public void visit(RaceLogTagEvent event) {
        Document object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        mongoObjectFactory.storeRaceLogEventEvent(object);
    }
}
