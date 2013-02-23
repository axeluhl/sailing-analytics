package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class TestStoringAndRetrievingRaceLogInLeaderboards extends AbstractMongoDBTest {
    public TestStoringAndRetrievingRaceLogInLeaderboards() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithRaceLogPassChangeEvent() {
    	TimePoint now = MillisecondsTimePoint.now();
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mof, dof);
        
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowPoint(), null);
        
        
        //Fleet[] fleets = {new FleetImpl("Gold"), new FleetImpl("Silver") };
        
        RaceColumn r1 = leaderboard.addRaceColumn("R1", /* medalRace */ false, leaderboard.getFleet(null));
        
        RaceLogPassChangeEvent event = RaceLogEventFactory.INSTANCE.createRaceLogPassChangeEvent(now, 0);
        
        r1.getRaceLog(leaderboard.getFleet(null)).add(event);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        
        RaceLog loadedRaceLog = loadedLeaderboard.getRaceColumnByName("R1").getRaceLog(loadedLeaderboard.getFleet(null));
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
    	TimePoint now = MillisecondsTimePoint.now();
        final String leaderboardName = "TestLeaderboard";
        final UUID uuid = UUID.randomUUID();
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mof, dof);
        
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowPoint(), null);
        
        
        //Fleet[] fleets = {new FleetImpl("Gold"), new FleetImpl("Silver") };
        
        RaceColumn r1 = leaderboard.addRaceColumn("R1", /* medalRace */ false, leaderboard.getFleet(null));
        
        RaceLogCourseAreaChangedEvent event = RaceLogEventFactory.INSTANCE.createRaceLogCourseAreaChangedEvent(now, 0, uuid);
        
        r1.getRaceLog(leaderboard.getFleet(null)).add(event);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        
        RaceLog loadedRaceLog = loadedLeaderboard.getRaceColumnByName("R1").getRaceLog(loadedLeaderboard.getFleet(null));
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
    	TimePoint now = MillisecondsTimePoint.now();
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mof, dof);
        
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowPoint(), null);
        
        
        //Fleet[] fleets = {new FleetImpl("Gold"), new FleetImpl("Silver") };
        
        RaceColumn r1 = leaderboard.addRaceColumn("R1", /* medalRace */ false, leaderboard.getFleet(null));
        
        RaceLogRaceStatusEvent event = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(now, 0, RaceLogRaceStatus.SCHEDULED);
        
        r1.getRaceLog(leaderboard.getFleet(null)).add(event);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        
        RaceLog loadedRaceLog = loadedLeaderboard.getRaceColumnByName("R1").getRaceLog(loadedLeaderboard.getFleet(null));
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
    	TimePoint now = MillisecondsTimePoint.now();
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mof, dof);
        
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowPoint(), null);
        
        
        //Fleet[] fleets = {new FleetImpl("Gold"), new FleetImpl("Silver") };
        
        RaceColumn r1 = leaderboard.addRaceColumn("R1", /* medalRace */ false, leaderboard.getFleet(null));
        
        RaceLogStartTimeEvent event = RaceLogEventFactory.INSTANCE.createStartTimeEvent(now, 0, RaceLogRaceStatus.RUNNING, now);
        
        r1.getRaceLog(leaderboard.getFleet(null)).add(event);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        
        RaceLog loadedRaceLog = loadedLeaderboard.getRaceColumnByName("R1").getRaceLog(loadedLeaderboard.getFleet(null));
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
    	TimePoint now = MillisecondsTimePoint.now();
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        MongoObjectFactory mof = MongoFactory.INSTANCE.getMongoObjectFactory(getMongoService());
        DomainObjectFactory dof = MongoFactory.INSTANCE.getDomainObjectFactory(getMongoService());
        RaceLogStore raceLogStore = MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mof, dof);
        
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(raceLogStore, leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowPoint(), null);
        
        
        //Fleet[] fleets = {new FleetImpl("Gold"), new FleetImpl("Silver") };
        
        RaceColumn r1 = leaderboard.addRaceColumn("R1", /* medalRace */ false, leaderboard.getFleet(null));
        
        RaceLogFlagEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(now, 0, Flags.FIRSTSUBSTITUTE, Flags.NONE, true);
        
        r1.getRaceLog(leaderboard.getFleet(null)).add(event);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        
        RaceLog loadedRaceLog = loadedLeaderboard.getRaceColumnByName("R1").getRaceLog(loadedLeaderboard.getFleet(null));
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
