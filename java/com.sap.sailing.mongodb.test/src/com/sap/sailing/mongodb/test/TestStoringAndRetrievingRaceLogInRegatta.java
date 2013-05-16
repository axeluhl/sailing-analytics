package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

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
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class TestStoringAndRetrievingRaceLogInRegatta extends RaceLogMongoDBTest {

    String raceColumnName = "My.First$Race$1";
    String regattaName = "TestRegatta";
    final String yellowFleetName = "Yellow";
    final String seriesName = "Qualifying";
    MongoObjectFactory mongoObjectFactory = null;
    DomainObjectFactory domainObjectFactory = null;
    Regatta regatta = null;

    public TestStoringAndRetrievingRaceLogInRegatta() throws UnknownHostException, MongoException {
        super();

    }

    @Before
    public void setUp() {
        now = MillisecondsTimePoint.now();
        mongoObjectFactory = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        domainObjectFactory = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());

        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);
        regatta = createRegattaAndAddRaceColumns(1, regattaName, boatClass, true, DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogStartProcedureChangedEvent() {        
        RaceLogStartProcedureChangedEvent expectedEvent = RaceLogEventFactory.INSTANCE.createStartProcedureChangedEvent(now, 0, StartProcedureType.ESS);

       addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogStartProcedureChangedEvent actualEvent = (RaceLogStartProcedureChangedEvent) loadedEvent;
            assertEquals(expectedEvent.getTimePoint(), actualEvent.getTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getStartProcedureType(), actualEvent.getStartProcedureType());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogPathfinderEvent() {        
        RaceLogPathfinderEvent expectedEvent = RaceLogEventFactory.INSTANCE.createPathfinderEvent(now, 0, "GER 20");

       addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogPathfinderEvent actualEvent = (RaceLogPathfinderEvent) loadedEvent;
            assertEquals(expectedEvent.getTimePoint(), actualEvent.getTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getPathfinderId(), actualEvent.getPathfinderId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogGateLineOpeningTimeEvent() {        
        RaceLogGateLineOpeningTimeEvent expectedEvent = RaceLogEventFactory.INSTANCE.createGateLineOpeningTimeEvent(now, 0, 1234l);

       addAndStoreRaceLogEvent(regatta, raceColumnName, expectedEvent);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogGateLineOpeningTimeEvent actualEvent = (RaceLogGateLineOpeningTimeEvent) loadedEvent;
            assertEquals(expectedEvent.getTimePoint(), actualEvent.getTimePoint());
            assertEquals(expectedEvent.getPassId(), actualEvent.getPassId());
            assertEquals(expectedEvent.getId(), actualEvent.getId());
            assertEquals(expectedEvent.getGateLineOpeningTime(), actualEvent.getGateLineOpeningTime());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogPassChangeEvent() {        
        RaceLogPassChangeEvent event = RaceLogEventFactory.INSTANCE.createPassChangeEvent(now, 0);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogPassChangeEvent passEvent = (RaceLogPassChangeEvent) loadedEvent;
            assertEquals(event.getTimePoint(), passEvent.getTimePoint());
            assertEquals(event.getPassId(), passEvent.getPassId());
            assertEquals(event.getId(), passEvent.getId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogFinishPositioningListChangeEvent() {
        Competitor storedCompetitor = DomainFactory.INSTANCE.createCompetitor(UUID.randomUUID(), "SAP Extreme Sailing Team", null, null);
        List<Triple<Serializable, String, MaxPointsReason>> storedPositioningList = new ArrayList<Triple<Serializable, String, MaxPointsReason>>();
        storedPositioningList.add(new Triple<Serializable, String, MaxPointsReason>(storedCompetitor.getId(), storedCompetitor.getName(), MaxPointsReason.NONE));
        
        RaceLogFinishPositioningListChangedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningListChangedEvent(now, 0, storedPositioningList);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFinishPositioningListChangedEvent loadedPositioningEvent = (RaceLogFinishPositioningListChangedEvent) loadedEvent;
            assertEquals(event.getTimePoint(), loadedPositioningEvent.getTimePoint());
            assertEquals(event.getPassId(), loadedPositioningEvent.getPassId());
            assertEquals(event.getId(), loadedPositioningEvent.getId());
            assertEquals(event.getInvolvedBoats().size(), loadedPositioningEvent.getInvolvedBoats().size());
            assertEquals(event.getPositionedCompetitors().size(), loadedPositioningEvent.getPositionedCompetitors().size());
            assertEquals(event.getPositionedCompetitors().get(0).getA(), loadedPositioningEvent.getPositionedCompetitors().get(0).getA());
            assertEquals(event.getPositionedCompetitors().get(0).getB(), loadedPositioningEvent.getPositionedCompetitors().get(0).getB());
            assertEquals(event.getPositionedCompetitors().get(0).getC().name(), loadedPositioningEvent.getPositionedCompetitors().get(0).getC().name());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogFinishPositioningConfirmedEvent() {        
        RaceLogFinishPositioningConfirmedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, 0);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFinishPositioningConfirmedEvent loadedConfirmedEvent = (RaceLogFinishPositioningConfirmedEvent) loadedEvent;
            assertEquals(event.getTimePoint(), loadedConfirmedEvent.getTimePoint());
            assertEquals(event.getPassId(), loadedConfirmedEvent.getPassId());
            assertEquals(event.getId(), loadedConfirmedEvent.getId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogCourseAreaChangeEvent() {
        final UUID uuid = UUID.randomUUID();
        RaceLogCourseAreaChangedEvent event = RaceLogEventFactory.INSTANCE.createCourseAreaChangedEvent(now, 0, uuid);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogCourseAreaChangedEvent courseAreaEvent = (RaceLogCourseAreaChangedEvent) loadedEvent;
            assertEquals(event.getTimePoint(), courseAreaEvent.getTimePoint());
            assertEquals(event.getPassId(), courseAreaEvent.getPassId());
            assertEquals(event.getId(), courseAreaEvent.getId());
            assertEquals(event.getCourseAreaId(), courseAreaEvent.getCourseAreaId());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogRaceStatusEvent() {

        RaceLogRaceStatusEvent event = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(now, 0, RaceLogRaceStatus.SCHEDULED);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) loadedEvent;
            assertEquals(event.getTimePoint(), statusEvent.getTimePoint());
            assertEquals(event.getPassId(), statusEvent.getPassId());
            assertEquals(event.getId(), statusEvent.getId());
            assertEquals(event.getNextStatus(), statusEvent.getNextStatus());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogStartTimeEvent() {

        RaceLogStartTimeEvent event = RaceLogEventFactory.INSTANCE.createStartTimeEvent(now, 0, now);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogStartTimeEvent timeEvent = (RaceLogStartTimeEvent) loadedEvent;
            assertEquals(event.getTimePoint(), timeEvent.getTimePoint());
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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogFlagEvent() {

        RaceLogFlagEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(now, 0, Flags.FIRSTSUBSTITUTE, Flags.NONE, true);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) loadedEvent;
            assertEquals(event.getTimePoint(), flagEvent.getTimePoint());
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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogCourseDesignChangedEvent() {
        CourseBase course = createCourseBase();
        RaceLogCourseDesignChangedEvent event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(now, 0, course);

        addAndStoreRaceLogEvent(regatta, raceColumnName, event);

        RaceLog loadedRaceLog = retrieveRaceLog();

        loadedRaceLog.lockForRead();
        try {
            RaceLogEvent loadedEvent = loadedRaceLog.getFirstRawFix();
            RaceLogCourseDesignChangedEvent courseDesignEvent = (RaceLogCourseDesignChangedEvent) loadedEvent;
            assertEquals(event.getTimePoint(), courseDesignEvent.getTimePoint());
            assertEquals(event.getPassId(), courseDesignEvent.getPassId());
            assertEquals(event.getId(), courseDesignEvent.getId());
            compareCourseData(event.getCourseDesign(), courseDesignEvent.getCourseDesign());
            assertEquals(1, Util.size(loadedRaceLog.getFixes()));
        } finally {
            loadedRaceLog.unlockAfterRead();
        }
    }

}
