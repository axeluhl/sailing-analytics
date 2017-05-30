package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.operationaltransformation.OperationalTransformer;
import com.sap.sse.operationaltransformation.Peer;
import com.sap.sse.operationaltransformation.Peer.Role;
import com.sap.sse.operationaltransformation.PeerImpl;


public class RaceLogInRaceColumnTest {

    private static final String LEADERBOARDNAME = "TESTBOARD";

    private RacingEventService racingEventServiceServer;
    private Peer<RacingEventServiceOperation<?>, RacingEventService> server;
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);

    @Before
    public void setUp() {
        MongoDBService.INSTANCE.getDB().dropDatabase();
        racingEventServiceServer = new RacingEventServiceImpl();
        OperationalTransformer<RacingEventService, RacingEventServiceOperation<?>> transformer = new OperationalTransformer<>();
        server = new PeerImpl<>(transformer, racingEventServiceServer, Role.SERVER);
    }

    @Test
    public void testAddEventToSingleRaceLog() throws InterruptedException {
        String raceColumnName = "myRaceColumn";

        RacingEventServiceOperation<FlexibleLeaderboard> addLeaderboardOp =
                new CreateFlexibleLeaderboard(
                        LEADERBOARDNAME, LEADERBOARDNAME, new int[] { 5 }, new LowPoint(), null);
        server.apply(addLeaderboardOp);

        RacingEventServiceOperation<RaceColumn> addLeaderboardColumn = 
                new AddColumnToLeaderboard(
                        raceColumnName, LEADERBOARDNAME, true);
        server.apply(addLeaderboardColumn);

        TimePoint t1 = MillisecondsTimePoint.now();
        RaceLogEvent rlEvent = new RaceLogFlagEventImpl(
                t1, author, 0, Flags.CLASS, Flags.NONE, true);

        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(LEADERBOARDNAME);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        Fleet fleet = raceColumn.getFleetByName("Default");
        RaceLog raceLog = raceColumn.getRaceLog(fleet);

        raceLog.add(rlEvent);

        // TODO: Why do we have to sleep?
        Thread.sleep(1000);
        try {
            raceLog.lockForRead();
            assertEquals(rlEvent, raceLog.getFirstRawFix());
        } finally {
            raceLog.unlockAfterRead();
        }
    }


}

/*
public class RaceLogInRacingEventServiceImplTest {

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
                "newColumn", LEADERBOARDNAME, true);
        server.apply(addLeaderboardColumn);

        RacingEventServiceOperation<RaceColumn> addLeaderboardColumn2 = new AddColumnToLeaderboard(
                "ColumnMy", LEADERBOARDNAME, false);
        server.apply(addLeaderboardColumn2);

        TimePoint t1 = MillisecondsTimePoint.now();
        TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() + 1000);
		int passId = 0;
		boolean isDisplayed = true;
		RaceLogFlagEvent rcEvent = new RaceLogFlagEventImpl(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		RaceLogFlagEvent rcEvent2 = new RaceLogFlagEventImpl(t2, passId, Flags.AP, Flags.NONE, isDisplayed);

        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(LEADERBOARDNAME);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName("newColumn");
        Fleet fleet = raceColumn.getFleetByName("Default");

        RaceLogIdentifier identifier = new RaceLogIdentifierImpl(leaderboard, raceColumn, fleet);

        RaceLog raceLog = racingEventServiceServer.getRaceLog(identifier);
        raceLog.add(rcEvent);

        Thread.sleep(1000);

        RaceLog raceLogAfterAdd = racingEventServiceServer.getRaceLog(identifier);
        raceLog.lockForRead();
        try {
        	raceLogAfterAdd.lockForRead();
        	try {
        		assertEquals(Util.size(raceLog.getFixes()), Util.size(raceLogAfterAdd.getFixes()));
        	} finally {
        		raceLogAfterAdd.unlockAfterRead();
        	}
        } finally {
        	raceLog.unlockAfterRead();
        }

        RaceColumn raceColumnMy = leaderboard.getRaceColumnByName("ColumnMy");
        Fleet fleetMy = raceColumnMy.getFleetByName("Default");
        RaceLogIdentifier identifierMy = new RaceLogIdentifierImpl(leaderboard, raceColumnMy, fleetMy);

        RaceLog raceLogMy = racingEventServiceServer.getRaceLog(identifierMy);
        raceLogMy.add(rcEvent);
        raceLogMy.add(rcEvent2);

        Thread.sleep(1000);

        RaceLog raceLogMyAfterAdd = racingEventServiceServer.getRaceLog(identifierMy);
        raceLogMy.lockForRead();
        try {
        	raceLogMyAfterAdd.lockForRead();
        	try {
        		assertEquals(Util.size(raceLogMy.getFixes()), Util.size(raceLogMyAfterAdd.getFixes()));
        	} finally {
        		raceLogMyAfterAdd.unlockAfterRead();
        	}
        	raceLog.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogMy.getFixes()));
        		assertEquals(1, Util.size(raceLog.getFixes()));
        	} finally {
        		raceLog.unlockAfterRead();
        	}
        } finally {
        	raceLogMy.unlockAfterRead();
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
		RaceLogFlagEvent rcEvent = new RaceLogFlagEventImpl(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		RaceLogFlagEvent rcEvent2 = new RaceLogFlagEventImpl(t2, passId, Flags.AP, Flags.NONE, isDisplayed);

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

        RaceColumn raceColumnR2 = leaderboard.getRaceColumnByName("R2");
        Fleet fleetR2 = raceColumnR2.getFleetByName("Default");
        RaceLogIdentifier identifierR2 = new RaceLogIdentifierImpl(leaderboard, raceColumnR2, fleetR2);

        RaceLog raceLogR2BeforeAdd = racingEventServiceServer.getRaceLog(identifierR2);
        raceLogR2BeforeAdd.add(rcEvent);
        raceLogR2BeforeAdd.add(rcEvent2);

        Thread.sleep(1000);

        RaceLog raceLogR2AfterAdd = racingEventServiceServer.getRaceLog(identifierR2);
        raceLogR2BeforeAdd.lockForRead();
        try {
        	raceLogR2AfterAdd.lockForRead();
        	try {
        		assertEquals(Util.size(raceLogR2BeforeAdd.getFixes()), Util.size(raceLogR2AfterAdd.getFixes()));
        	} finally {
        		raceLogR2AfterAdd.unlockAfterRead();
        	}
        	raceLog.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogR2BeforeAdd.getFixes()));
        		assertEquals(1, Util.size(raceLog.getFixes()));
        	} finally {
        		raceLog.unlockAfterRead();
        	}
        } finally {
        	raceLogR2BeforeAdd.unlockAfterRead();
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
		RaceLogFlagEvent rcEvent = new RaceLogFlagEventImpl(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		RaceLogFlagEvent rcEvent2 = new RaceLogFlagEventImpl(t2, passId, Flags.AP, Flags.NONE, isDisplayed);

        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(regattaLeaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName("R1");
        Fleet fleet = raceColumn.getFleetByName("Default");

        RaceLogIdentifier identifierR1 = new RaceLogIdentifierImpl(leaderboard, raceColumn, fleet);

        RaceLog raceLogR1 = racingEventServiceServer.getRaceLog(identifierR1);

        server.apply(new RecordRaceLogEvent(identifierR1, rcEvent));

        Thread.sleep(1000);
        RaceLog raceLogR1_2 = racingEventServiceServer.getRaceLog(identifierR1);
        raceLogR1.lockForRead();
        try {
        	raceLogR1_2.lockForRead();
        	try {
        		assertEquals(1, Util.size(raceLogR1.getFixes()));
        		assertEquals(1, Util.size(raceLogR1_2.getFixes()));
        	} finally {
        		raceLogR1_2.unlockAfterRead();
        	}
        } finally {
        	raceLogR1.unlockAfterRead();
        }

        RaceColumn raceColumnR2 = leaderboard.getRaceColumnByName("R2");
        Fleet fleetR2 = raceColumnR2.getFleetByName("Default");

        RaceLogIdentifier identifierR2 = new RaceLogIdentifierImpl(leaderboard, raceColumnR2, fleetR2);

        RaceLog raceLogR2 = racingEventServiceServer.getRaceLog(identifierR2);

        server.apply(new RecordRaceLogEvent(identifierR2, rcEvent));
        server.apply(new RecordRaceLogEvent(identifierR2, rcEvent2));

        Thread.sleep(1000);

        RaceLog raceLogR2AfterAdd = racingEventServiceServer.getRaceLog(identifierR2);
        raceLogR2AfterAdd.lockForRead();
        try {
        	raceLogR2.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogR2.getFixes()));
        		assertEquals(2, Util.size(raceLogR2AfterAdd.getFixes()));
        	} finally {
        		raceLogR2.unlockAfterRead();
        	}
        	raceLogR1_2.lockForRead();
        	try {
        		assertEquals(2, Util.size(raceLogR2AfterAdd.getFixes()));
        		assertEquals(1, Util.size(raceLogR1_2.getFixes()));
        	} finally {
        		raceLogR1_2.unlockAfterRead();
        	}
        } finally {
        	raceLogR2AfterAdd.unlockAfterRead();
        }
	}

}*/
