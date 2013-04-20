package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogPathfinderEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class TestStoringAndRetrievingRaceLogInLeaderboards extends RaceLogMongoDBTest {

    String leaderboardName = "TestLeaderboard";
    final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
    FlexibleLeaderboardImpl leaderboard = null;

    public TestStoringAndRetrievingRaceLogInLeaderboards() throws UnknownHostException, MongoException {
        super();

    }

    @Before
    public void setUp() {
        now = MillisecondsTimePoint.now();
        mongoObjectFactory = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        domainObjectFactory = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());

        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory);

        leaderboard = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowPoint(), null);

        Fleet defaultFleet = leaderboard.getFleet(null);
        leaderboard.addRaceColumn(raceColumnName, /* medalRace */ false, defaultFleet);
    }

    private void addAndStoreRaceLogEvent(FlexibleLeaderboard leaderboard, String raceColumnName, RaceLogEvent event) {
        Fleet defaultFleet = leaderboard.getFleet(null);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        raceColumn.getRaceLog(defaultFleet).add(event);

        mongoObjectFactory.storeLeaderboard(leaderboard);
    }

    private RaceLog retrieveRaceLog() {
        Leaderboard loadedLeaderboard = domainObjectFactory.loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        Fleet loadedDefaultFleet = loadedLeaderboard.getFleet(null);

        return loadedLeaderboard.getRaceColumnByName(raceColumnName).getRaceLog(loadedDefaultFleet);
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogPathfinderEvent() {        
        RaceLogPathfinderEvent expectedEvent = RaceLogEventFactory.INSTANCE.createPathfinderEvent(now, 0, "GER 20");

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, expectedEvent);

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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogPassChangeEvent() {        
        RaceLogPassChangeEvent event = RaceLogEventFactory.INSTANCE.createPassChangeEvent(now, 0);

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogFinishPositioningConfirmedEvent() {        
        RaceLogFinishPositioningConfirmedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, 0);

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogFinishPositioningListChangeEvent() {
        Competitor storedCompetitor = DomainFactory.INSTANCE.createCompetitor(UUID.randomUUID(), "SAP Extreme Sailing Team", null, null);
        List<Triple<Serializable, String, MaxPointsReason>> storedPositioningList = new ArrayList<Triple<Serializable, String, MaxPointsReason>>();
        storedPositioningList.add(new Triple<Serializable, String, MaxPointsReason>(storedCompetitor.getId(), storedCompetitor.getName(), MaxPointsReason.NONE));
        
        RaceLogFinishPositioningListChangedEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningListChangedEvent(now, 0, storedPositioningList);
        
        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogCourseAreaChangeEvent() {
        final UUID uuid = UUID.randomUUID();
        RaceLogCourseAreaChangedEvent event = RaceLogEventFactory.INSTANCE.createCourseAreaChangedEvent(now, 0, uuid);

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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

        addAndStoreRaceLogEvent(leaderboard, raceColumnName, event);

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
