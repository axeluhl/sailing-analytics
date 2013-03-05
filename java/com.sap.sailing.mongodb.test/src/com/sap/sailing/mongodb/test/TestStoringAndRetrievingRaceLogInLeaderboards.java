package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class TestStoringAndRetrievingRaceLogInLeaderboards extends AbstractMongoDBTest {

    String raceColumnName = "My.First$Race$1";
    String leaderboardName = "TestLeaderboard";
    final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
    TimePoint now = null;
    MongoObjectFactory mongoObjectFactory = null;
    DomainObjectFactory domainObjectFactory = null;
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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogPassChangeEvent() {        
        RaceLogPassChangeEvent event = RaceLogEventFactory.INSTANCE.createRaceLogPassChangeEvent(now, 0);

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
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogCourseAreaChangeEvent() {
        final UUID uuid = UUID.randomUUID();
        RaceLogCourseAreaChangedEvent event = RaceLogEventFactory.INSTANCE.createRaceLogCourseAreaChangedEvent(now, 0, uuid);

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

}
