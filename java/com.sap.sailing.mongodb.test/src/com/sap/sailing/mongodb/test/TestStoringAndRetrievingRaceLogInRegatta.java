package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoUtils;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceLogWindFixEvent;
import com.sap.sailing.domain.racelog.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.tracking.Wind;

public class TestStoringAndRetrievingRaceLogInRegatta extends RaceLogMongoDBTest {

    String raceColumnName = "My.First$Race$1";
    String regattaName = "TestRegatta";
    final String yellowFleetName = "Yellow";
    final String seriesName = "Qualifying";
    MongoObjectFactory mongoObjectFactory = null;
    DomainObjectFactory domainObjectFactory = null;
    Regatta regatta = null;
    private RaceLogEventAuthor author = new RaceLogEventAuthorImpl("Test Author", 1);

    public TestStoringAndRetrievingRaceLogInRegatta() throws UnknownHostException, MongoException {
        super();
    }

    @Before
    public void setUp() {
        now = MillisecondsTimePoint.now();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(getMongoService(), DomainFactory.INSTANCE);

        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        regatta = createRegattaAndAddRaceColumns(1, regattaName, boatClass, true,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
    }
    
    private Regatta createRegattaAndAddRaceColumns(final int numberOfQualifyingRaces, final String regattaBaseName, BoatClass boatClass, boolean persistent, ScoringScheme scoringScheme) {
        Regatta regatta = createRegattaWithoutRaceColumns(regattaBaseName, boatClass, persistent, scoringScheme, null);
        regatta.getSeriesByName(seriesName).addRaceColumn(raceColumnName, /* trackedRegattaRegistry */ null);
        
        return regatta;
    }
    
    private Regatta createRegattaWithoutRaceColumns(final String regattaBaseName, BoatClass boatClass, boolean persistent, ScoringScheme scoringScheme, CourseArea courseArea) {
        List<String> emptyRaceColumnNames = Collections.emptyList();
        List<Series> series = new ArrayList<Series>();
        
        // -------- qualifying series ------------
        List<Fleet> qualifyingFleets = new ArrayList<Fleet>();
        qualifyingFleets.add(new FleetImpl(yellowFleetName));
        Series qualifyingSeries = new SeriesImpl(seriesName, /* isMedal */false, qualifyingFleets,
                emptyRaceColumnNames, /* trackedRegattaRegistry */ null);
        series.add(qualifyingSeries);
        
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory);
        
        Regatta regatta = new RegattaImpl(raceLogStore, regattaBaseName, boatClass, series, persistent, scoringScheme, "123", courseArea);
        return regatta;
    }
    
    private void addAndStoreRaceLogEvent(Regatta regatta, String raceColumnName, RaceLogEvent event) {
        Series series = regatta.getSeriesByName(seriesName);
        Fleet fleet = series.getFleetByName(yellowFleetName);
        RaceColumn raceColumn = series.getRaceColumnByName(raceColumnName);
        raceColumn.getRaceLog(fleet).add(event);
        
        mongoObjectFactory.storeRegatta(regatta);
    }

    private RaceLog retrieveRaceLog() {
        Regatta loadedRegatta = domainObjectFactory.loadRegatta(regatta.getName(), /* trackedRegattaRegistry */ null);
        Series loadedSeries = loadedRegatta.getSeriesByName(seriesName);
        Fleet loadedFleet = loadedSeries.getFleetByName(yellowFleetName);
       
        return loadedSeries.getRaceColumnByName(raceColumnName).getRaceLog(loadedFleet);
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogProtestStartTimeEvent() {        
        RaceLogProtestStartTimeEvent expectedEvent = RaceLogEventFactory.INSTANCE.createProtestStartTimeEvent(now, author, 0, MillisecondsTimePoint.now());

        addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogProtestStartTimeEvent actualEvent = (RaceLogProtestStartTimeEvent) loadedEvent;
            assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getProtestStartTime(), actualEvent.getProtestStartTime());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogStartProcedureChangedEvent() {        
        RaceLogStartProcedureChangedEvent expectedEvent = RaceLogEventFactory.INSTANCE.createStartProcedureChangedEvent(now, author, 0, RacingProcedureType.ESS);

       addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogStartProcedureChangedEvent actualEvent = (RaceLogStartProcedureChangedEvent) loadedEvent;
            assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getStartProcedureType(), actualEvent.getStartProcedureType());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogPathfinderEvent() {        
        RaceLogPathfinderEvent expectedEvent = RaceLogEventFactory.INSTANCE.createPathfinderEvent(now, author, 0, "GER 20");

       addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogPathfinderEvent actualEvent = (RaceLogPathfinderEvent) loadedEvent;
            assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getPathfinderId(), actualEvent.getPathfinderId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogGateLineOpeningTimeEvent() {        
        RaceLogGateLineOpeningTimeEvent expectedEvent = RaceLogEventFactory.INSTANCE.createGateLineOpeningTimeEvent(now, author, 0, 1234l, 54321l);

       addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogGateLineOpeningTimeEvent actualEvent = (RaceLogGateLineOpeningTimeEvent) loadedEvent;
            assertEquals(expectedEvent.getLogicalTimePoint(), actualEvent.getLogicalTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getGateLineOpeningTimes(), actualEvent.getGateLineOpeningTimes());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogPassChangeEvent() {        
        RaceLogPassChangeEvent event = RaceLogEventFactory.INSTANCE.createPassChangeEvent(now, author, 0);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogPassChangeEvent passEvent = (RaceLogPassChangeEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), passEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), passEvent.getPassId());
            assertEquals(event.getId(), passEvent.getId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogFinishPositioningListChangeEvent() {
        Competitor storedCompetitor = DomainFactory.INSTANCE.getOrCreateCompetitor(UUID.randomUUID(), "SAP Extreme Sailing Team", Color.RED, null, null);
        CompetitorResults storedPositioningList = new CompetitorResultsImpl();
        storedPositioningList.add(new Triple<Serializable, String, MaxPointsReason>(storedCompetitor.getId(), storedCompetitor.getName(), MaxPointsReason.NONE));
        
        RaceLogFinishPositioningListChangedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningListChangedEvent(now, author, 0, storedPositioningList);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFinishPositioningListChangedEvent loadedPositioningEvent = (RaceLogFinishPositioningListChangedEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), loadedPositioningEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), loadedPositioningEvent.getPassId());
            assertEquals(event.getId(), loadedPositioningEvent.getId());
            assertEquals(event.getInvolvedBoats().size(), loadedPositioningEvent.getInvolvedBoats().size());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().size(), loadedPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getA(), loadedPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getA());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getB(), loadedPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getB());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getC().name(), loadedPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getC().name());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogFinishPositioningConfirmedEvent() {   
        Competitor storedCompetitor = DomainFactory.INSTANCE.getOrCreateCompetitor(UUID.randomUUID(), "SAP Extreme Sailing Team", Color.RED, null, null);
        CompetitorResults storedPositioningList = new CompetitorResultsImpl();
        storedPositioningList.add(new Triple<Serializable, String, MaxPointsReason>(storedCompetitor.getId(), storedCompetitor.getName(), MaxPointsReason.NONE));
        
        RaceLogFinishPositioningConfirmedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, author, 0, storedPositioningList);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFinishPositioningConfirmedEvent loadedConfirmedEvent = (RaceLogFinishPositioningConfirmedEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), loadedConfirmedEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), loadedConfirmedEvent.getPassId());
            assertEquals(event.getId(), loadedConfirmedEvent.getId());
            assertEquals(event.getInvolvedBoats().size(), loadedConfirmedEvent.getInvolvedBoats().size());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().size(), loadedConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getA(), loadedConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getA());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getB(), loadedConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getB());
            assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getC().name(), loadedConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getC().name());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithBackwardsCompatibleRaceLogFinishPositioningConfirmedEvent() {
        RaceLogFinishPositioningConfirmedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, author, 0, null);

        createAndStoreOldRaceLogFinishPositioningConfirmedEventDBEntry();

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFinishPositioningConfirmedEvent loadedConfirmedEvent = (RaceLogFinishPositioningConfirmedEvent) loadedEvent;
            assertEquals(now, loadedConfirmedEvent.getLogicalTimePoint());
            assertEquals(0, loadedConfirmedEvent.getPassId());
            assertEquals(0, loadedConfirmedEvent.getInvolvedBoats().size());
            assertNull(event.getPositionedCompetitorsIDsNamesMaxPointsReasons()); 
            assertNull(loadedConfirmedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    private void createAndStoreOldRaceLogFinishPositioningConfirmedEventDBEntry() {
        Series series = regatta.getSeriesByName(seriesName);
        Fleet fleet = series.getFleetByName(yellowFleetName);
        RaceColumn raceColumn = series.getRaceColumnByName(raceColumnName);
        mongoObjectFactory.storeRegatta(regatta);
        
        DBObject result = new BasicDBObject();
        result.put(FieldNames.TIME_AS_MILLIS.name(), now.asMillis());
        result.put(FieldNames.RACE_LOG_EVENT_CREATED_AT.name(), now.asMillis());
        result.put(FieldNames.RACE_LOG_EVENT_ID.name(), UUID.randomUUID());
        result.put(FieldNames.RACE_LOG_EVENT_PASS_ID.name(), 0);
        result.put(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name(), new BasicDBList());
        result.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogFinishPositioningConfirmedEvent.class.getSimpleName());
        
        DBObject raceLogResult = new BasicDBObject();
        raceLogResult.put(FieldNames.RACE_LOG_IDENTIFIER.name(), MongoUtils.escapeDollarAndDot(raceColumn.getRaceLogIdentifier(fleet).getDeprecatedIdentifier()));       
        raceLogResult.put(FieldNames.RACE_LOG_EVENT.name(), result);
        
        MongoObjectFactoryImpl factoryImpl = (MongoObjectFactoryImpl) mongoObjectFactory;
        factoryImpl.getRaceLogCollection().insert(raceLogResult);
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogCourseAreaChangeEvent() {
        final UUID uuid = UUID.randomUUID();
        RaceLogCourseAreaChangedEvent event = RaceLogEventFactory.INSTANCE.createCourseAreaChangedEvent(now, author, 0, uuid);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogCourseAreaChangedEvent courseAreaEvent = (RaceLogCourseAreaChangedEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), courseAreaEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), courseAreaEvent.getPassId());
            assertEquals(event.getId(), courseAreaEvent.getId());
            assertEquals(event.getCourseAreaId(), courseAreaEvent.getCourseAreaId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogRaceStatusEvent() {

        RaceLogRaceStatusEvent event = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(now, author, 0, RaceLogRaceStatus.SCHEDULED);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), statusEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), statusEvent.getPassId());
            assertEquals(event.getId(), statusEvent.getId());
            assertEquals(event.getNextStatus(), statusEvent.getNextStatus());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogStartTimeEvent() {

        RaceLogStartTimeEvent event = RaceLogEventFactory.INSTANCE.createStartTimeEvent(now, author, 0, now);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogStartTimeEvent timeEvent = (RaceLogStartTimeEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), timeEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), timeEvent.getPassId());
            assertEquals(event.getId(), timeEvent.getId());
            assertEquals(event.getNextStatus(), timeEvent.getNextStatus());
            assertEquals(event.getStartTime(), timeEvent.getStartTime());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogFlagEvent() {

        RaceLogFlagEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(now, author, 0, Flags.FIRSTSUBSTITUTE, Flags.NONE, true);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), flagEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), flagEvent.getPassId());
            assertEquals(event.getId(), flagEvent.getId());
            assertEquals(event.getUpperFlag(), flagEvent.getUpperFlag());
            assertEquals(event.getLowerFlag(), flagEvent.getLowerFlag());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogCourseDesignChangedEvent() {
        CourseBase course = createCourseBase();
        RaceLogCourseDesignChangedEvent event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(now, author, 0, course);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogCourseDesignChangedEvent courseDesignEvent = (RaceLogCourseDesignChangedEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), courseDesignEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), courseDesignEvent.getPassId());
            assertEquals(event.getId(), courseDesignEvent.getId());
            compareCourseData(event.getCourseDesign(), courseDesignEvent.getCourseDesign());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveRegattaWithRaceLogWindFixEvent() {
        Wind wind = createWindFix();
        RaceLogWindFixEvent event = RaceLogEventFactory.INSTANCE.createWindFixEvent(now, author, 0, wind);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogWindFixEvent windEvent = (RaceLogWindFixEvent) loadedEvent;
            assertEquals(event.getLogicalTimePoint(), windEvent.getLogicalTimePoint());
            assertEquals(event.getPassId(), windEvent.getPassId());
            assertEquals(event.getId(), windEvent.getId());
            compareWind(event.getWindFix(), windEvent.getWindFix());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

}
