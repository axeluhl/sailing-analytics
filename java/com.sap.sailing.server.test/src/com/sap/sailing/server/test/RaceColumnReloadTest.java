package com.sap.sailing.server.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogWindFixEventImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoRaceLogStoreVisitor;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddDefaultRegatta;
import com.sap.sailing.server.operationaltransformation.AddRaceDefinition;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateTrackedRace;
import com.sap.sailing.server.operationaltransformation.TrackRegatta;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.mongodb.MongoDBService;

import junit.framework.Assert;

public class RaceColumnReloadTest {
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);

    private RaceColumn raceColumn;
    private RaceLog raceLog;
    private RaceLogWindFixEventImpl testWindEvent1;
    private MongoObjectFactoryImpl objectFactory;
    private RaceLogIdentifier raceLogIdentifier;
    private RaceLogWindFixEventImpl testWindEvent2;
    private MongoRaceLogStoreVisitor mongoStoreVisitor;

    private RacingEventServiceImpl service;

    private RegattaNameAndRaceName raceIdentifier;

    private DynamicTrackedRace trackedRace;

    private Fleet defaultFleet;

    @Before
    public void setUp() {
        MongoDBService.INSTANCE.getDB().dropDatabase();
        this.service = new RacingEventServiceImpl();

        objectFactory = new MongoObjectFactoryImpl(MongoDBService.INSTANCE.getDB());

        final String boatClassName = "49er";
        // FIXME use master DomainFactory; see bug 592
        final DomainFactory masterDomainFactory = service.getBaseDomainFactory();
        BoatClass boatClass = masterDomainFactory.getOrCreateBoatClass(boatClassName, /* typicallyStartsUpwind */true);
        Competitor competitor = createCompetitor(masterDomainFactory);
        int[] discardThreshold = { 1, 2 };
        CreateFlexibleLeaderboard createLeaderboardOperation = new CreateFlexibleLeaderboard("Test Leaderboard", "Test",
                discardThreshold, new LowPoint(), null);
        service.apply(createLeaderboardOperation);
        AddColumnToLeaderboard leaderboardColumnOperation = new AddColumnToLeaderboard("R1", "Test Leaderboard", false);
        raceColumn = service.apply(leaderboardColumnOperation);

        final String baseRegattaName = "Test Event";
        AddDefaultRegatta addRegattaOperation = new AddDefaultRegatta(
                RegattaImpl.getDefaultName(baseRegattaName, boatClassName), boatClassName, /* startDate */ null,
                /* endDate */ null, UUID.randomUUID());
        Regatta regatta = service.apply(addRegattaOperation);
        final String raceName = "Test Race";
        final CourseImpl masterCourse = new CourseImpl("Test Course", new ArrayList<Waypoint>());
        RaceDefinition race = new RaceDefinitionImpl(raceName, masterCourse, boatClass,
                Collections.singletonList(competitor));
        AddRaceDefinition addRaceOperation = new AddRaceDefinition(new RegattaName(regatta.getName()), race);
        service.apply(addRaceOperation);
        masterCourse.addWaypoint(0, masterDomainFactory.createWaypoint(masterDomainFactory.getOrCreateMark("Mark1"),
                /* passingInstruction */ null));
        raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), raceName);
        service.apply(new TrackRegatta(raceIdentifier));
        trackedRace = (DynamicTrackedRace) service.apply(new CreateTrackedRace(raceIdentifier, EmptyWindStore.INSTANCE,
                /* delayToLiveInMillis */ 5000, /* millisecondsOverWhichToAverageWind */ 10000,
                /* millisecondsOverWhichToAverageSpeed */10000));
        trackedRace.setStartOfTrackingReceived(MillisecondsTimePoint.now());
        defaultFleet = Util.get(raceColumn.getFleets(), 0);

        raceLogIdentifier = raceColumn.getRaceLogIdentifier(defaultFleet);
        raceLog = raceColumn.getRaceLog(defaultFleet);

        trackedRace.attachRaceLog(raceLog);

        mongoStoreVisitor = new MongoRaceLogStoreVisitor(raceLogIdentifier, objectFactory);

        TimePoint t1 = MillisecondsTimePoint.now();

        Wind wind1 = new WindImpl(/* position */ null, t1, new KnotSpeedWithBearingImpl(1, new DegreeBearingImpl(10)));
        testWindEvent1 = new RaceLogWindFixEventImpl(t1, author, 0, wind1, false);

        TimePoint t2 = MillisecondsTimePoint.now().plus(1000);
        Wind wind2 = new WindImpl(/* position */ null, t1, new KnotSpeedWithBearingImpl(2, new DegreeBearingImpl(20)));
        testWindEvent2 = new RaceLogWindFixEventImpl(t2, author, 0, wind2, false);

    }

    private Competitor createCompetitor(final DomainFactory masterDomainFactory) {
        return masterDomainFactory.getOrCreateCompetitor("GER 61", "Sailor", Color.RED, "noone@nowhere.de", null,
                new TeamImpl("Sailor",
                        (List<PersonImpl>) Arrays.asList(new PersonImpl[] { new PersonImpl("Sailor 1",
                                DomainFactory.INSTANCE.getOrCreateNationality("GER"), null, null) }),
                        new PersonImpl("Sailor 2", DomainFactory.INSTANCE.getOrCreateNationality("NED"), null, null)),
                new BoatImpl("GER 61",
                        DomainFactory.INSTANCE.getOrCreateBoatClass("470", /* typicallyStartsUpwind */ true), "GER 61"),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowanceInSecondsPerNauticalMile */ null, null);
    }

    @Test
    public void testWindAddedOnlyViaDB() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        AtomicInteger raceCommiteeSeenWindEvents = new AtomicInteger(0);
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void windDataReceived(Wind wind, WindSource windSource) {
                if (WindSourceType.RACECOMMITTEE == windSource.getType()) {
                    raceCommiteeSeenWindEvents.incrementAndGet();
                }
            }
        });

        mongoStoreVisitor.visit(testWindEvent1);
        mongoStoreVisitor.visit(testWindEvent2);
        Assert.assertEquals(raceCommiteeSeenWindEvents.get(), seenWindEvents.get());

        raceColumn.reloadRaceLog(defaultFleet);
        Assert.assertEquals(2, seenWindEvents.get());
        Assert.assertEquals(raceCommiteeSeenWindEvents.get(), seenWindEvents.get());
    }

    @Test
    public void testWindAddedAndDbWithDifferentAddedReloadWithTrackedRace() throws InterruptedException {
        AtomicInteger seenWindEvents = new AtomicInteger(0);
        raceLog.addListener(new BaseRaceLogEventVisitor() {
            @Override
            public void visit(RaceLogWindFixEvent event) {
                seenWindEvents.incrementAndGet();
            }
        });
        AtomicInteger raceCommiteeSeenWindEvents = new AtomicInteger(0);
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void windDataReceived(Wind wind, WindSource windSource) {
                System.out.println("Wind data " + wind + " " + windSource);
                if (WindSourceType.RACECOMMITTEE == windSource.getType()) {
                    raceCommiteeSeenWindEvents.incrementAndGet();
                }
            }

            @Override
            public void raceLogAttached(RaceLog raceLog) {
                System.out.println("RaceLog attached");
            }
        });

        raceLog.add(testWindEvent1);

        Assert.assertEquals(raceCommiteeSeenWindEvents.get(), seenWindEvents.get());

        mongoStoreVisitor.visit(testWindEvent2);
        Assert.assertEquals(raceCommiteeSeenWindEvents.get(), seenWindEvents.get());

        raceColumn.reloadRaceLog(defaultFleet);
        Assert.assertEquals(2, seenWindEvents.get());
        Assert.assertEquals(raceCommiteeSeenWindEvents.get(), seenWindEvents.get());
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
        raceColumn.reloadRaceLog(defaultFleet);
        raceLog.add(testWindEvent1);
        raceColumn.reloadRaceLog(defaultFleet);
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
        raceColumn.reloadRaceLog(defaultFleet);
        raceLog.add(testWindEvent2);
        raceColumn.reloadRaceLog(defaultFleet);
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

        mongoStoreVisitor.visit(testWindEvent1);
        raceColumn.reloadRaceLog(defaultFleet);
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

        mongoStoreVisitor.visit(testWindEvent2);
        Assert.assertEquals(1, seenWindEvents.get());

        raceColumn.reloadRaceLog(defaultFleet);
        Assert.assertEquals(2, seenWindEvents.get());
    }

}
