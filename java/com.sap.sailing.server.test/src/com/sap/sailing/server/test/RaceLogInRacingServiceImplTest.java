package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogIdentifierImpl;
import com.sap.sailing.mongodb.MongoDBService;
import com.sap.sailing.operationaltransformation.Peer;
import com.sap.sailing.operationaltransformation.PeerImpl;
import com.sap.sailing.operationaltransformation.Peer.Role;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.OperationalTransformer;
import com.sap.sailing.server.operationaltransformation.RecordRaceLogEvent;

public class RaceLogInRacingServiceImplTest {

	private static final String LEADERBOARDNAME = "TESTBOARD";
	
	private RacingEventService racingEventServiceServer;
	private Peer<RacingEventServiceOperation<?>, RacingEventService> server;
	
	@Before
	public void setUp() {
		MongoDBService.INSTANCE.getDB().dropDatabase();
        racingEventServiceServer = new RacingEventServiceImpl();
        OperationalTransformer transformer = new OperationalTransformer();
        server = new PeerImpl<>(transformer, racingEventServiceServer, Role.SERVER);
	}

	@Test
	public void testRecordingOfRaceLogEventsForDifferentRaces() throws InterruptedException {
		RacingEventServiceOperation<FlexibleLeaderboard> addLeaderboardOp = new CreateFlexibleLeaderboard(LEADERBOARDNAME, new int[] { 5 },
                new LowPoint(), null);
		server.apply(addLeaderboardOp);
		
		RacingEventServiceOperation<RaceColumn> addLeaderboardColumn = new AddColumnToLeaderboard(
                "newColumn", LEADERBOARDNAME, /* medalRace */ true);
        server.apply(addLeaderboardColumn);
        
        RacingEventServiceOperation<RaceColumn> addLeaderboardColumn2 = new AddColumnToLeaderboard(
                "arminColumn", LEADERBOARDNAME, /* medalRace */ false);
        server.apply(addLeaderboardColumn2);
        
        TimePoint t1 = MillisecondsTimePoint.now();
        TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() + 1000);
		int passId = 0;
		boolean isDisplayed = true;
		RaceLogFlagEvent rcEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t2, passId, Flags.AP, Flags.NONE, isDisplayed);
        
        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(LEADERBOARDNAME);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName("newColumn");
        Fleet fleet = raceColumn.getFleetByName("Default");
        
        RaceLogIdentifier identifier = new RaceLogIdentifierImpl(leaderboard, raceColumn, fleet);
        
        RaceLog raceLog = racingEventServiceServer.getRaceLog(identifier);
        raceLog.add(rcEvent);
        
        Thread.sleep(1000);
        
        RaceLog raceLog2 = racingEventServiceServer.getRaceLog(identifier);
        raceLog.lockForRead();
        try {
        	raceLog2.lockForRead();
        	try {
        		assertEquals(Util.size(raceLog.getFixes()), Util.size(raceLog2.getFixes()));
        	} finally {
        		raceLog2.unlockAfterRead();
        	}
        } finally {
        	raceLog.unlockAfterRead();
        }
        
        RaceColumn arminColumn = leaderboard.getRaceColumnByName("arminColumn");
        Fleet fleetarmin = arminColumn.getFleetByName("Default");
        RaceLogIdentifier identifierArmin = new RaceLogIdentifierImpl(leaderboard, arminColumn, fleetarmin);
        
        RaceLog raceLogArmin = racingEventServiceServer.getRaceLog(identifierArmin);
        raceLogArmin.add(rcEvent);
        raceLogArmin.add(rcEvent2);
        
        Thread.sleep(1000);
        
        RaceLog raceLogArmin2 = racingEventServiceServer.getRaceLog(identifierArmin);
        raceLogArmin.lockForRead();
        try {
        	raceLogArmin2.lockForRead();
        	try {
        		assertEquals(Util.size(raceLogArmin.getFixes()), Util.size(raceLogArmin2.getFixes()));
        	} finally {
        		raceLogArmin2.unlockAfterRead();
        	}
        	raceLog.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogArmin.getFixes()));
        		assertEquals(1, Util.size(raceLog.getFixes()));
        	} finally {
        		raceLog.unlockAfterRead();
        	}
        } finally {
        	raceLogArmin.unlockAfterRead();
        }
	}
	
	@Test
	public void testRecordingOfRaceLogEventsWithRegattaLeaderboard() throws InterruptedException {
		String regattaName = "IDM 2013-Default";
		String regattaLeaderboardName = RegattaImpl.getFullName(regattaName, "Dragon");
		RacingEventServiceOperation<Regatta> addDefaultRegatta = new AddDefaultRegatta(regattaName, "Dragon", UUID.randomUUID());
		server.apply(addDefaultRegatta);
		
		Regatta regatta = racingEventServiceServer.getRegattaByName(regattaLeaderboardName);
		Series series = regatta.getSeriesByName("Default");
		series.addRaceColumn("R1", racingEventServiceServer);
		series.addRaceColumn("R2", racingEventServiceServer);
		
		RacingEventServiceOperation<RegattaLeaderboard> addLeaderboardOp = 
				new CreateRegattaLeaderboard(regatta.getRegattaIdentifier(), new int[] { 5 }, null);
		server.apply(addLeaderboardOp);
		
        
        TimePoint t1 = MillisecondsTimePoint.now();
        TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() + 1000);
		int passId = 0;
		boolean isDisplayed = true;
		RaceLogFlagEvent rcEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t2, passId, Flags.AP, Flags.NONE, isDisplayed);
        
        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(regattaLeaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName("R1");
        Fleet fleet = raceColumn.getFleetByName("Default");
        
        RaceLogIdentifier identifierR1 = new RaceLogIdentifierImpl(leaderboard, raceColumn, fleet);
        
        RaceLog raceLog = racingEventServiceServer.getRaceLog(identifierR1);
        raceLog.add(rcEvent);
        
        Thread.sleep(1000);
        
        RaceLog raceLog2 = racingEventServiceServer.getRaceLog(identifierR1);
        raceLog.lockForRead();
        try {
        	raceLog2.lockForRead();
        	try {
        		assertEquals(Util.size(raceLog.getFixes()), Util.size(raceLog2.getFixes()));
        	} finally {
        		raceLog2.unlockAfterRead();
        	}
        } finally {
        	raceLog.unlockAfterRead();
        }
        
        RaceColumn arminColumn = leaderboard.getRaceColumnByName("R2");
        Fleet fleetarmin = arminColumn.getFleetByName("Default");
        RaceLogIdentifier identifierR2 = new RaceLogIdentifierImpl(leaderboard, arminColumn, fleetarmin);
        
        RaceLog raceLogArmin = racingEventServiceServer.getRaceLog(identifierR2);
        raceLogArmin.add(rcEvent);
        raceLogArmin.add(rcEvent2);
        
        Thread.sleep(1000);
        
        RaceLog raceLogArmin2 = racingEventServiceServer.getRaceLog(identifierR2);
        raceLogArmin.lockForRead();
        try {
        	raceLogArmin2.lockForRead();
        	try {
        		assertEquals(Util.size(raceLogArmin.getFixes()), Util.size(raceLogArmin2.getFixes()));
        	} finally {
        		raceLogArmin2.unlockAfterRead();
        	}
        	raceLog.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogArmin.getFixes()));
        		assertEquals(1, Util.size(raceLog.getFixes()));
        	} finally {
        		raceLog.unlockAfterRead();
        	}
        } finally {
        	raceLogArmin.unlockAfterRead();
        }
	}
	
	@Test
	public void testRecordingOfRaceLogEventOverOperationalTransformationCommand() throws InterruptedException {
		String regattaName = "IDM 2013-Default";
		String regattaLeaderboardName = RegattaImpl.getFullName(regattaName, "Dragon");
		RacingEventServiceOperation<Regatta> addDefaultRegatta = new AddDefaultRegatta(regattaName, "Dragon", UUID.randomUUID());
		server.apply(addDefaultRegatta);
		
		Regatta regatta = racingEventServiceServer.getRegattaByName(regattaLeaderboardName);
		Series series = regatta.getSeriesByName("Default");
		series.addRaceColumn("R1", racingEventServiceServer);
		series.addRaceColumn("R2", racingEventServiceServer);
		
		RacingEventServiceOperation<RegattaLeaderboard> addLeaderboardOp = 
				new CreateRegattaLeaderboard(regatta.getRegattaIdentifier(), new int[] { 5 }, null);
		server.apply(addLeaderboardOp);
		
        
        TimePoint t1 = MillisecondsTimePoint.now();
        TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() + 1000);
		int passId = 0;
		boolean isDisplayed = true;
		RaceLogFlagEvent rcEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t2, passId, Flags.AP, Flags.NONE, isDisplayed);
        
        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(regattaLeaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName("R1");
        Fleet fleet = raceColumn.getFleetByName("Default");
        
        RaceLogIdentifier identifierR1 = new RaceLogIdentifierImpl(leaderboard, raceColumn, fleet);
        
        RaceLog raceLog = racingEventServiceServer.getRaceLog(identifierR1);
        
        server.apply(new RecordRaceLogEvent(identifierR1, rcEvent));
        
        Thread.sleep(1000);
        RaceLog raceLog2 = racingEventServiceServer.getRaceLog(identifierR1);
        raceLog.lockForRead();
        try {
        	raceLog2.lockForRead();
        	try {
        		assertEquals(0, Util.size(raceLog.getFixes()));
        		assertEquals(1, Util.size(raceLog2.getFixes()));
        	} finally {
        		raceLog2.unlockAfterRead();
        	}
        } finally {
        	raceLog.unlockAfterRead();
        }
        
        RaceColumn arminColumn = leaderboard.getRaceColumnByName("R2");
        Fleet fleetarmin = arminColumn.getFleetByName("Default");
        
        RaceLogIdentifier identifierR2 = new RaceLogIdentifierImpl(leaderboard, arminColumn, fleetarmin);
        
        RaceLog raceLogArmin = racingEventServiceServer.getRaceLog(identifierR2);
        
        server.apply(new RecordRaceLogEvent(identifierR2, rcEvent));
        server.apply(new RecordRaceLogEvent(identifierR2, rcEvent2));
        
        Thread.sleep(1000);
        
        RaceLog raceLogArmin2 = racingEventServiceServer.getRaceLog(identifierR2);
        raceLogArmin2.lockForRead();
        try {
        	raceLogArmin.lockForRead();
        	try {
        		assertEquals(0, Util.size(raceLogArmin.getFixes()));
        		assertEquals(2, Util.size(raceLogArmin2.getFixes()));
        	} finally {
        		raceLogArmin.unlockAfterRead();
        	}
        	raceLog2.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogArmin2.getFixes()));
        		assertEquals(1, Util.size(raceLog2.getFixes()));
        	} finally {
        		raceLog2.unlockAfterRead();
        	}
        } finally {
        	raceLogArmin2.unlockAfterRead();
        }
	}
}
