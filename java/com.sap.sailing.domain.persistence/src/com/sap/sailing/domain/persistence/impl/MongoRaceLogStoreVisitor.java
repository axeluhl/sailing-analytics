package com.sap.sailing.domain.persistence.impl;

import java.util.logging.Logger;

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
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.racelog.tracking.DeviceMarkMappingEvent;
import com.sap.sailing.domain.racelog.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;

public class MongoRaceLogStoreVisitor implements RaceLogEventVisitor {

    private final RaceLogIdentifier raceLogIdentifier;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    private final DBCollection raceLogsCollection;
    
    private final static Logger logger = Logger.getLogger(MongoRaceLogStoreVisitor.class.getName());

    public MongoRaceLogStoreVisitor(RaceLogIdentifier identifier, MongoObjectFactory mongoObjectFactory) {
        super();
        this.raceLogIdentifier = identifier;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
        this.raceLogsCollection = this.mongoObjectFactory.getRaceLogCollection();
    }

    private void storeEventInCollection(DBObject eventEntry) {
        raceLogsCollection.insert(eventEntry);
        logger.fine("Inserted event entry into mongo race log collection");
    }

    @Override
    public void visit(RaceLogFlagEvent event) {
        DBObject flagEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(flagEventTrackEntry);
    }

    @Override
    public void visit(RaceLogPassChangeEvent event) {
        DBObject passChangeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(passChangeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogRaceStatusEvent event) {
        DBObject raceStatusEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(raceStatusEventTrackEntry);
    }

    @Override
    public void visit(RaceLogStartTimeEvent event) {
        DBObject startTimeEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(startTimeEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseAreaChangedEvent event) {
        DBObject courseAreaChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(courseAreaChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogCourseDesignChangedEvent event) {
        DBObject courseDesignChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(courseDesignChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningListChangedEvent event) {
        DBObject finishPositioningListChangedEventTrackEntry = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(finishPositioningListChangedEventTrackEntry);
    }

    @Override
    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
    }

    @Override
    public void visit(RaceLogPathfinderEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
    }

    @Override
    public void visit(RaceLogGateLineOpeningTimeEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
    }

    @Override
    public void visit(RaceLogStartProcedureChangedEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
    }

    @Override
    public void visit(RaceLogProtestStartTimeEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
    }

    @Override
    public void visit(RaceLogWindFixEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
    }

	@Override
	public void visit(DeviceCompetitorMappingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
	}

	@Override
	public void visit(DeviceMarkMappingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
	}

	@Override
	public void visit(DenoteForTrackingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
	}

	@Override
	public void visit(StartTrackingEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
	}

	@Override
	public void visit(RevokeEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
	}

	@Override
	public void visit(RegisterCompetitorEvent event) {
        DBObject object = mongoObjectFactory.storeRaceLogEntry(raceLogIdentifier, event);
        storeEventInCollection(object);
	}

}
