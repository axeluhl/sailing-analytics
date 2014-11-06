package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboard;
import com.sap.sailing.server.replication.ReplicationMasterDescriptor;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBService;

public class RaceLogReplicationTest extends AbstractServerReplicationTest {
    private static final String BOAT_CLASS_NAME_49er = "49er";

    private com.sap.sse.common.Util.Pair<ReplicationServiceTestImpl, ReplicationMasterDescriptor> replicationDescriptorPair;
    
    private RaceLogEvent raceLogEvent;
    private RaceLogEvent anotherRaceLogEvent;
    private RaceLogEventAuthor author = new RaceLogEventAuthorImpl("Test Author", 1);
    
    @Before
    public void setUp() throws Exception {
        raceLogEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(new MillisecondsTimePoint(1), author, 42, RaceLogRaceStatus.UNKNOWN);
        anotherRaceLogEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(new MillisecondsTimePoint(2), author, 42, RaceLogRaceStatus.UNKNOWN);
        try {
            replicationDescriptorPair = basicSetUp(true, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            tearDown();
        }
    }
    
    @Test
    public void testRaceLogEmptyOnInitialLoad() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        Regatta regatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RaceLog masterLog = setupRaceColumn(regatta, seriesName, raceColumnName, fleetName);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, regatta);
        addAndValidateEventIds(masterLog, replicaLog);
    }
    
    @Test
    public void testRaceLogStateOnInitialLoad() throws InterruptedException, ClassNotFoundException, IOException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        Regatta masterRegatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        masterLog.add(raceLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidateEventIds(masterLog, replicaLog);
    }
    
    @Test
    public void testRaceEventReplicationOnEmptyRegatta() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        Regatta masterRegatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidateEventIds(masterLog, replicaLog, raceLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationOnEmptyFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        addAndValidateEventIds(masterLog, replicaLog, raceLogEvent);
    }

    @Test
    public void testRaceEventReplicationOnRegatta() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        Regatta masterRegatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        masterLog.add(raceLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidateEventIds(masterLog, replicaLog, anotherRaceLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationCourseDesignOnRegatta() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        Regatta masterRegatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        raceLogEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(MillisecondsTimePoint.now(), author, 43, createCourseData());
        masterLog.add(raceLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        anotherRaceLogEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(MillisecondsTimePoint.now(), author, 43, createCourseData());
        addAndValidateEventIds(masterLog, replicaLog, anotherRaceLogEvent);
        compareReplicatedCourseDesignEvent(replicaLog, (RaceLogCourseDesignChangedEvent) anotherRaceLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationOnFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        masterLog.add(raceLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        addAndValidateEventIds(masterLog, replicaLog, anotherRaceLogEvent);
    }
    
    @Test
    public void testRaceEventReplicationCourseDesignOnFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        raceLogEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(MillisecondsTimePoint.now(), author, 43, createCourseData());
        masterLog.add(raceLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        anotherRaceLogEvent = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(MillisecondsTimePoint.now(), author, 43, createCourseData());
        addAndValidateEventIds(masterLog, replicaLog, anotherRaceLogEvent);
        compareReplicatedCourseDesignEvent(replicaLog, (RaceLogCourseDesignChangedEvent) anotherRaceLogEvent);
    }
    
    private void compareReplicatedCourseDesignEvent(RaceLog replicaLog, RaceLogCourseDesignChangedEvent courseDesignChangedEvent) {
        replicaLog.lockForRead();
        try {
            RaceLogCourseDesignChangedEvent replicatedEvent = (RaceLogCourseDesignChangedEvent) replicaLog.getLastRawFix();
            assertEquals(courseDesignChangedEvent.getId(), replicatedEvent.getId());
            assertEquals(courseDesignChangedEvent.getPassId(), replicatedEvent.getPassId());
            assertEquals(courseDesignChangedEvent.getCreatedAt(), replicatedEvent.getCreatedAt());
            assertEquals(courseDesignChangedEvent.getLogicalTimePoint(), replicatedEvent.getLogicalTimePoint());
            assertEquals(Util.size(courseDesignChangedEvent.getInvolvedBoats()), Util.size(replicatedEvent.getInvolvedBoats()));
            compareCourseBase(courseDesignChangedEvent.getCourseDesign(), replicatedEvent.getCourseDesign());
        } finally {
            replicaLog.unlockAfterRead();
        }
    }

    @Ignore
    public void testRaceEventReplicationOnRenamingFlexibleLeaderboard() throws ClassNotFoundException, IOException, InterruptedException {
        final String leaderboardName = "Test";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        FlexibleLeaderboard masterLeaderboard = setupFlexibleLeaderboard(leaderboardName);
        RaceLog masterLog = setupRaceColumn(leaderboardName, fleetName, raceColumnName);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        masterLog.add(raceLogEvent);
        RenameLeaderboard renameOperation = new RenameLeaderboard(leaderboardName, leaderboardName + "new");
        master.apply(renameOperation);
        Thread.sleep(3000);
        RaceLog replicaLog = getReplicaLog(fleetName, raceColumnName, masterLeaderboard);
        addAndValidateEventIds(masterLog, replicaLog, anotherRaceLogEvent);
    }
    
    /**
     * See bug 1666; a race log reload operation may not properly have been replicating. This tests asserts that when a race log event
     * has been added to the DB and then race log is re-loaded on the master, the events added through the DB also show up on the replica.
     */
    @Test
    public void testRaceLogReloadReplication() throws ClassNotFoundException, IOException, InterruptedException {
        final String regattaName = "Test";
        final String seriesName = "Default";
        final String fleetName = "Default";
        final String raceColumnName = "R1";
        Regatta masterRegatta = setupRegatta(RegattaImpl.getDefaultName(regattaName, BOAT_CLASS_NAME_49er), seriesName, fleetName, BOAT_CLASS_NAME_49er);
        RaceLog masterLog = setupRaceColumn(masterRegatta, seriesName, raceColumnName, fleetName);
        masterLog.add(raceLogEvent);
        replicationDescriptorPair.getA().startToReplicateFrom(replicationDescriptorPair.getB());
        RaceLog replicaLog = getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta);
        addAndValidateEventIds(masterLog, replicaLog);
        
        final Series series = masterRegatta.getSeries().iterator().next();
        final MillisecondsTimePoint approximateRaceStatusEventCreationTimePoint = MillisecondsTimePoint.now();
        final RaceLogRaceStatusEvent raceStatusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(approximateRaceStatusEventCreationTimePoint, author, 42,
                RaceLogRaceStatus.UNKNOWN);
        addEventToDB(series.getRaceColumnByName(raceColumnName).getRaceLogIdentifier(series.getFleetByName(fleetName)), raceStatusEvent, regattaName, raceColumnName, fleetName);
        RegattaLeaderboard leaderboard = master.apply(new CreateRegattaLeaderboard(
                masterRegatta.getRegattaIdentifier(), /* leaderboardDisplayName */ null, new int[0]));
        master.reloadRaceLog(leaderboard.getName(), raceColumnName, fleetName);
        Thread.sleep(3000);
        final RaceLog reloadedMasterLog = leaderboard.getRaceColumnByName(raceColumnName).getRaceLog(series.getFleetByName(fleetName));
        final RaceLogEvent lastEventReadFromMasterLog = reloadedMasterLog.getFirstRawFixAtOrAfter(approximateRaceStatusEventCreationTimePoint);
        assertNotNull(lastEventReadFromMasterLog);
        assertEqualsOnId(reloadedMasterLog, getReplicaLog(seriesName, fleetName, raceColumnName, masterRegatta));
    }
    
    /**
     * Uses a new master that loads the existing regatta and race log to append an event to the race log. This will store it to the DB so that if the original
     * master re-loads the race log it should see the new race log event.
     */
    private void addEventToDB(RaceLogIdentifier raceLogIdentifier, RaceLogRaceStatusEvent createRaceStatusEvent, String regattaName, String raceColumnName, String fleetName) {
        final MongoObjectFactory defaultMongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
        defaultMongoObjectFactory.getDatabase().getLastError(); // wait for regatta write operation to complete
        final RacingEventServiceImpl temporaryMaster = createNewMaster(MongoDBService.INSTANCE, defaultMongoObjectFactory);
        Regatta regatta = temporaryMaster.getRegatta(new RegattaName(regattaName+" ("+BOAT_CLASS_NAME_49er+")"));
        Series series = regatta.getSeries().iterator().next();
        RaceLog masterLog = series.getRaceColumnByName(raceColumnName).getRaceLog(series.getFleetByName(fleetName));
        masterLog.add(createRaceStatusEvent);
        defaultMongoObjectFactory.getDatabase().getLastError(); // wait for write operation to complete
    }

    /**
     * Validation is done based only on the identifier and type of the {@link RaceLogEvent}s.
     */
    private void addAndValidateEventIds(RaceLog masterLog, RaceLog replicaLog, RaceLogEvent... addedEvents) throws InterruptedException {
        // 1. Check state of replica after initial load...
        assertEqualsOnId(masterLog, replicaLog);
        // 2. ... add all incoming events...
        for (RaceLogEvent event : addedEvents) {
            masterLog.add(event);
        }
        // 3. ... and give replication some time to deliver messages.
        Thread.sleep(3000);
        assertEqualsOnId(masterLog, replicaLog);
    }

    /**
     * Equality of the events is done based only on the identifier and type.
     */
    private void assertEqualsOnId(RaceLog masterLog, RaceLog replicaLog) {
        replicaLog.lockForRead();
        try {
            masterLog.lockForRead();
            try {
                assertEqualsOnId(masterLog.getRawFixes(), replicaLog.getRawFixes());
            } finally {
                masterLog.unlockAfterRead();
            }
        } finally {
            replicaLog.unlockAfterRead();
        }
    }

    /**
     * Equality of the events is done based only on the {@link RaceLogEvent}'s identifier and type.
     */
    private void assertEqualsOnId(Iterable<RaceLogEvent> expectedEvents, Iterable<RaceLogEvent> actualEvents) {
        List<RaceLogEvent> expectedCollection = new ArrayList<>();
        Util.addAll(expectedEvents, expectedCollection);
        List<RaceLogEvent> actualCollection = new ArrayList<>();
        Util.addAll(actualEvents, actualCollection);
        assertEquals(Util.size(expectedEvents), Util.size(actualEvents));
        for (int i = 0; i < expectedCollection.size(); i++) {
            assertEquals(expectedCollection.get(i).getClass(), actualCollection.get(i).getClass());
            assertEquals(expectedCollection.get(i).getId(), actualCollection.get(i).getId());
        }
    }

    private RaceLog getReplicaLog(final String fleetName, final String raceColumnName, Leaderboard leaderboard) {
        Leaderboard replicaLeaderboard = replica.getLeaderboardByName(leaderboard.getName());
        RaceColumn replicaColumn = replicaLeaderboard.getRaceColumnByName(raceColumnName);
        Fleet replicaFleet = replicaColumn.getFleetByName(fleetName);
        return replicaColumn.getRaceLog(replicaFleet);
    }

    private RaceLog getReplicaLog(final String seriesName, final String fleetName, final String raceColumnName,
            Regatta masterRegatta) {
        Regatta replicaRegatta = replica.getRegatta(masterRegatta.getRegattaIdentifier());
        RaceColumn replicaColumn = replicaRegatta.getSeriesByName(seriesName).getRaceColumnByName(raceColumnName);
        Fleet replicaFleet = replicaColumn.getFleetByName(fleetName);
        return replicaColumn.getRaceLog(replicaFleet);
    }

    private Regatta setupRegatta(final String regattaName, String seriesName, String fleetName, String boatClassName) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters = new LinkedHashMap<>();
        SeriesCreationParametersDTO creationParametersForDefaultSeries = new SeriesCreationParametersDTO(
                Arrays.asList(new FleetDTO[] { new FleetDTO(fleetName, 0, Color.BLACK), }), /* medal */false, /* startsWithZero */
                false, /* firstColumnIsNonDiscardableCarryForward */false, /* discardingThresholds */new int[0], /* hasSplitFleetContiguousScoring */
                false);
        seriesCreationParameters.put(seriesName, creationParametersForDefaultSeries);
        // 1. Install some race column on master...
        RegattaCreationParametersDTO regattaCreationParams = new RegattaCreationParametersDTO(seriesCreationParameters);
        AddSpecificRegatta addRegattaOperation = new AddSpecificRegatta(regattaName, boatClassName, /* regatta ID */ UUID.randomUUID(), regattaCreationParams, /* persistent */ true,
                new LowPoint(), /* default course area ID */ UUID.randomUUID(), /* useStartTimeInference */ true);
        return master.apply(addRegattaOperation);
    }
    
    private RaceLog setupRaceColumn(final Regatta regatta, final String seriesName, 
            final String raceColumnName, final String fleetName) {
        AddColumnToSeries addColumnOperation = new AddColumnToSeries(regatta.getRegattaIdentifier(), seriesName, raceColumnName);
        RaceColumn raceColumn = master.apply(addColumnOperation);
        Fleet masterFleet = raceColumn.getFleetByName(fleetName);
        return raceColumn.getRaceLog(masterFleet);
    }

    private FlexibleLeaderboard setupFlexibleLeaderboard(final String leaderboardName) {
        CreateFlexibleLeaderboard createTestLeaderboard = new CreateFlexibleLeaderboard(leaderboardName, leaderboardName, new int[] { 19, 44 }, new LowPoint(), null);
        FlexibleLeaderboard masterLeaderboard = master.apply(createTestLeaderboard);
        return masterLeaderboard;
    }
    
    private RaceLog setupRaceColumn(final String leaderboardName, final String fleetName, final String raceColumnName) {
        AddColumnToLeaderboard addColumnOperation = new AddColumnToLeaderboard(raceColumnName, leaderboardName, false);
        RaceColumn masterRaceColumn = master.apply(addColumnOperation);
        Fleet masterFleet = masterRaceColumn.getFleetByName(fleetName);
        RaceLog masterLog = masterRaceColumn.getRaceLog(masterFleet);
        return masterLog;
    }
    
    protected CourseBase createCourseData() {
        CourseBase course = new CourseDataImpl("Test Course");
        course.addWaypoint(0, new WaypointImpl(new ControlPointWithTwoMarksImpl(UUID.randomUUID(), 
                new MarkImpl(UUID.randomUUID(), "Black", MarkType.BUOY, "black", "round", "circle"),
                new MarkImpl(UUID.randomUUID(), "Green", MarkType.BUOY, "green", "round", "circle"),
                "Upper gate")));
        course.addWaypoint(1, new WaypointImpl(new MarkImpl(UUID.randomUUID(), "White", MarkType.BUOY, "white", "conical", "bold"), PassingInstruction.Port));
        
        return course;
    }
    
    protected void compareCourseBase(CourseBase masterCourse, CourseBase replicatedCourse) {
        assertEquals(masterCourse.getFirstWaypoint().getPassingInstructions(), PassingInstruction.None);
        assertEquals(replicatedCourse.getFirstWaypoint().getPassingInstructions(), PassingInstruction.None);
        Assert.assertTrue(masterCourse.getFirstWaypoint().getControlPoint() instanceof ControlPointWithTwoMarks);
        Assert.assertTrue(replicatedCourse.getFirstWaypoint().getControlPoint() instanceof ControlPointWithTwoMarks);
        
        ControlPointWithTwoMarks masterGate = (ControlPointWithTwoMarks) masterCourse.getFirstWaypoint().getControlPoint();
        ControlPointWithTwoMarks replicatedGate = (ControlPointWithTwoMarks) replicatedCourse.getFirstWaypoint().getControlPoint();
        
        assertEquals(masterGate.getId(), replicatedGate.getId());
        assertEquals(masterGate.getName(), replicatedGate.getName());
        
        compareMarks(masterGate.getLeft(), replicatedGate.getLeft());
        compareMarks(masterGate.getRight(), replicatedGate.getRight());
        
        assertEquals(masterCourse.getLastWaypoint().getPassingInstructions(), PassingInstruction.Port);
        assertEquals(replicatedCourse.getLastWaypoint().getPassingInstructions(), PassingInstruction.Port);
        Assert.assertTrue(masterCourse.getLastWaypoint().getControlPoint() instanceof Mark);
        Assert.assertTrue(replicatedCourse.getLastWaypoint().getControlPoint() instanceof Mark);
        
        Mark masterMark = (Mark) masterCourse.getLastWaypoint().getControlPoint();
        Mark replicatedMark = (Mark) replicatedCourse.getLastWaypoint().getControlPoint();
        compareMarks(masterMark, replicatedMark);
    }
    
    private void compareMarks(Mark masterMark, Mark replicatedMark) {
        assertEquals(masterMark.getId(), replicatedMark.getId());
        assertEquals(masterMark.getColor(), replicatedMark.getColor());
        assertEquals(masterMark.getName(), replicatedMark.getName());
        assertEquals(masterMark.getPattern(), replicatedMark.getPattern());
        assertEquals(masterMark.getShape(), replicatedMark.getShape());
        assertEquals(masterMark.getType(), replicatedMark.getType());
    }
    
}
