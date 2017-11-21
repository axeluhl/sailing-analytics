package com.sap.sailing.server.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogWindFixEventImpl;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoRaceLogStoreVisitor;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
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

import junit.framework.Assert;

public class RaceColumnReloadTest {

    private static final String LEADERBOARDNAME = "TESTBOARD";

    private RacingEventService racingEventServiceServer;
    private Peer<RacingEventServiceOperation<?>, RacingEventService> server;
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);

    private RaceColumn raceColumn;

    private RaceLog raceLog;

    private RaceLogWindFixEventImpl testWindEvent1;

    private Fleet fleet;

    private MongoObjectFactoryImpl objectFactory;

    private RaceLogIdentifier raceLogIdentifier;

    private RaceLogWindFixEventImpl testWindEvent2;

    private MongoRaceLogStoreVisitor mongoStorer;

    @Before
    public void setUp() {
        MongoDBService.INSTANCE.getDB().dropDatabase();
        objectFactory = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());
        racingEventServiceServer = new RacingEventServiceImpl();
        OperationalTransformer<RacingEventService, RacingEventServiceOperation<?>> transformer = new OperationalTransformer<>();
        server = new PeerImpl<>(transformer, racingEventServiceServer, Role.SERVER);

        String raceColumnName = "myRaceColumn";
        RacingEventServiceOperation<FlexibleLeaderboard> addLeaderboardOp = new CreateFlexibleLeaderboard(
                LEADERBOARDNAME, LEADERBOARDNAME, new int[] { 5 }, new LowPoint(), null);
        server.apply(addLeaderboardOp);

        RacingEventServiceOperation<RaceColumn> addLeaderboardColumn = new AddColumnToLeaderboard(raceColumnName,
                LEADERBOARDNAME, true);
        server.apply(addLeaderboardColumn);

        Leaderboard leaderboard = racingEventServiceServer.getLeaderboardByName(LEADERBOARDNAME);
        raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        fleet = raceColumn.getFleetByName("Default");
        raceLog = raceColumn.getRaceLog(fleet);

        raceLogIdentifier = raceColumn.getRaceLogIdentifier(fleet);

        mongoStorer = new MongoRaceLogStoreVisitor(raceLogIdentifier,objectFactory);
        
        TimePoint t1 = MillisecondsTimePoint.now();
        Wind wind1 = new WindImpl(/* position */ null, t1, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70)));
        testWindEvent1 = new RaceLogWindFixEventImpl(t1, author, 0, wind1, false);
        
        TimePoint t2 = MillisecondsTimePoint.now().plus(1000);
        Wind wind2 = new WindImpl(/* position */ null, t1, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70)));
        testWindEvent2 = new RaceLogWindFixEventImpl(t2, author, 0, wind2, false);
    }

    @Test
    public void testAddedWind() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        raceLog.add(testWindEvent1);

        Assert.assertEquals(1, seenWindEvents.get());
    }

    @Test
    public void testAddedWindAndReloadAndTheSameWindMerged() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        raceLog.add(testWindEvent1);
        raceColumn.reloadRaceLog(fleet);
        raceLog.add(testWindEvent1);
        raceColumn.reloadRaceLog(fleet);
        Assert.assertEquals(1, seenWindEvents.get());
    }
    
    @Test
    public void testAddedWindAndReloadAndAddAnotherAndReloadAgain() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        raceLog.add(testWindEvent1);
        raceColumn.reloadRaceLog(fleet);
        raceLog.add(testWindEvent2);
        raceColumn.reloadRaceLog(fleet);
        Assert.assertEquals(2, seenWindEvents.get());
    }

    @Test
    public void testWindAddedAndDbWithSameAndReload() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        raceLog.add(testWindEvent1);
        Assert.assertEquals(1, seenWindEvents.get());

        mongoStorer.visit(testWindEvent1);
        raceColumn.reloadRaceLog(fleet);
        Assert.assertEquals(1, seenWindEvents.get());
    }

    @Test
    public void testWindAddedAndDbWithDifferentAndReload() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        raceLog.add(testWindEvent1);
        Assert.assertEquals(1, seenWindEvents.get());
        
        mongoStorer.visit(testWindEvent2);
        Assert.assertEquals(1, seenWindEvents.get());
        
        raceColumn.reloadRaceLog(fleet);
        Assert.assertEquals(2, seenWindEvents.get());
    }
}
