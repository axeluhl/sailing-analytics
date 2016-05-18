package com.sap.sailing.domain.racelogtracking.test.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelogtracking.test.AbstractGPSFixStoreTest;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackedRaceStartTimeInferenceTest extends AbstractGPSFixStoreTest {
    private final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");

    /**
     * tests the precedence order described in {@link TrackedRaceImpl#updateStartAndEndOfTracking(boolean)}
     */
    @Test
    public void testStartTimeInferencePrecedenceOrder() throws TransformationException,
            NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null, null,
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author, new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author, new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] { new WaypointImpl(mark),
                new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] { comp, comp2 }));

        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass, /* startDate */
                null, /* endDate */null, null, null, "a", null));
        final DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race,
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, store, 0, 0, 0, /*useMarkPassingCalculator*/ false,
                OneDesignRankingMetric::new, mock(RaceLogResolver.class));
        
        setStartAndEndOfRaceInRaceLog(10000, 20000);
        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);
        
        // test inference via race start/end time in racelog
        trackedRace.waitForLoadingFromGPSFixStoreToFinishRunning(regattaLog);
        MillisecondsTimePoint startOfRaceInRaceLog = new MillisecondsTimePoint(10000);
        TimePoint expectedStartOfTracking = startOfRaceInRaceLog.minus(Duration.ONE_MINUTE.times(TrackedRaceImpl.TRACKING_BUFFER_IN_MINUTES));
        MillisecondsTimePoint endOfRaceInRaceLog = new MillisecondsTimePoint(20000);
        TimePoint expectedEndOfTracking = endOfRaceInRaceLog.plus(Duration.ONE_MINUTE.times(TrackedRaceImpl.TRACKING_BUFFER_IN_MINUTES));
        assertEquals(expectedStartOfTracking, trackedRace.getStartOfTracking());
        assertEquals(expectedEndOfTracking, trackedRace.getEndOfTracking());
        
        // test inference via manually set start/end of tracking
        setManualTrackingTimesOnTrackedRace(trackedRace, 30000, 40000);
        assertEquals(new MillisecondsTimePoint(30000), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(40000), trackedRace.getEndOfTracking());
        
        // shouldn't change when setting time in raceLog again
        setStartAndEndOfRaceInRaceLog(100000, 200000);
        assertEquals(new MillisecondsTimePoint(30000), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(40000), trackedRace.getEndOfTracking());
        
        // test inference via start/end of tracking in RaceLog
        setStartAndEndOfTrackingInRaceLog(50000, 60000);
        MillisecondsTimePoint startOfTrackingInRacelog = new MillisecondsTimePoint(50000);
        MillisecondsTimePoint endOfTrackingInRacelog = new MillisecondsTimePoint(60000);
        assertEquals(startOfTrackingInRacelog, trackedRace.getStartOfTracking());
        assertEquals(endOfTrackingInRacelog, trackedRace.getEndOfTracking());
        
        // shouldn't change when setting time in raceLog again
        setStartAndEndOfRaceInRaceLog(300000, 400000);
        assertEquals(startOfTrackingInRacelog, trackedRace.getStartOfTracking());
        assertEquals(endOfTrackingInRacelog, trackedRace.getEndOfTracking());
        
        // shouldn't change when setting start/end of tracking
        setManualTrackingTimesOnTrackedRace(trackedRace, 500000, 600000);
        assertEquals(startOfTrackingInRacelog, trackedRace.getStartOfTracking());
        assertEquals(endOfTrackingInRacelog, trackedRace.getEndOfTracking());
    }    
    
    /**
     * tests notification of first start time update through race log; see bug 3660
     */
    @Test
    public void testStartTimeChangeNotificationForFirstUpdateThroughRaceLog() throws TransformationException,
            NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null, null,
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author, new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author, new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] { new WaypointImpl(mark),
                new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] { comp, comp2 }));
        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass, /* startDate */
                null, /* endDate */null, null, null, "a", null));
        final DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race,
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, store, 0, 0, 0, /*useMarkPassingCalculator*/ false,
                OneDesignRankingMetric::new, mock(RaceLogResolver.class));
        trackedRace.attachRaceLog(raceLog);
        final TimePoint[] oldAndNewStartTimeNotifiedByRace = new TimePoint[2];
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
                oldAndNewStartTimeNotifiedByRace[0] = oldStartOfRace;
                oldAndNewStartTimeNotifiedByRace[1] = newStartOfRace;
            }
        });
        assertNull(trackedRace.getStartOfRace());
        final TimePoint newStartOfRace = MillisecondsTimePoint.now();
        raceLog.add(new RaceLogStartTimeEventImpl(newStartOfRace, author, 0, newStartOfRace));
        assertNull(oldAndNewStartTimeNotifiedByRace[0]);
        assertEquals(newStartOfRace, oldAndNewStartTimeNotifiedByRace[1]);
        assertEquals(newStartOfRace, trackedRace.getStartOfRace());
    }    
    
    /**
     * tests notification of first start time update through race log; see bug 3660
     */
    @Test
    public void testStartTimeChangeNotificationForFirstUpdateThroughStartMarkPassing() throws TransformationException,
            NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null, null,
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author, new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author, new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] { new WaypointImpl(mark),
                new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] { comp, comp2 }));
        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass, /* startDate */
                null, /* endDate */null, null, null, "a", null));
        final DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race,
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, store, 0, 0, 0, /*useMarkPassingCalculator*/ false,
                OneDesignRankingMetric::new, mock(RaceLogResolver.class));
        final TimePoint[] oldAndNewStartTimeNotifiedByRace = new TimePoint[2];
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
                oldAndNewStartTimeNotifiedByRace[0] = oldStartOfRace;
                oldAndNewStartTimeNotifiedByRace[1] = newStartOfRace;
            }
        });
        assertNull(trackedRace.getStartOfRace());
        final TimePoint newStartOfRace = MillisecondsTimePoint.now();
        trackedRace.updateMarkPassings(comp, Arrays.asList(new MarkPassingImpl(newStartOfRace, Util.get(course.getWaypoints(),  0), comp),
                new MarkPassingImpl(newStartOfRace.plus(Duration.ONE_MINUTE), Util.get(course.getWaypoints(),  1), comp)));
        assertNull(oldAndNewStartTimeNotifiedByRace[0]);
        assertEquals(newStartOfRace, oldAndNewStartTimeNotifiedByRace[1]);
        assertEquals(newStartOfRace, trackedRace.getStartOfRace());
    }    
    

    
    
    /**
     * tests the precedence order described in {@link TrackedRaceImpl#updateStartAndEndOfTracking(boolean)}
     */
    @Test
    public void testStartAndEndTrackingTimeInferencePrecedenceOrderTriggersListener() throws TransformationException,
            NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null, null,
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author, new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author, new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] { new WaypointImpl(mark),
                new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] { comp, comp2 }));
        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass, /* startDate */
                null, /* endDate */null, null, null, "a", null));
        assertTrue(regatta.getRegatta().useStartTimeInference());
        final DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race,
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, store, 0, 0, 0, /*useMarkPassingCalculator*/ false,
                OneDesignRankingMetric::new, mock(RaceLogResolver.class));
        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRegattaLog(regattaLog);
        final TimePoint[] newStartAndEndOfTrackingNotifiedByRace = new TimePoint[2];
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void startOfTrackingChanged(TimePoint startOfTracking) {
                newStartAndEndOfTrackingNotifiedByRace[0] = startOfTracking;
            }
            @Override
            public void endOfTrackingChanged(TimePoint endOfTracking) {
                newStartAndEndOfTrackingNotifiedByRace[1] = endOfTracking;
            }
        });
        assertNull(trackedRace.getStartOfTracking());
        assertNull(trackedRace.getEndOfTracking());

        // test inference from implicit startOfRace change through start mark passing update
        newStartAndEndOfTrackingNotifiedByRace[0] = null;
        newStartAndEndOfTrackingNotifiedByRace[1] = null;
        final TimePoint startMarkPassingTimePoint = MillisecondsTimePoint.now();
        trackedRace.updateMarkPassings(comp, Collections.singleton(new MarkPassingImpl(startMarkPassingTimePoint, trackedRace.getRace().getCourse().getFirstWaypoint(), comp)));
        assertNotNull(newStartAndEndOfTrackingNotifiedByRace[0]);
        assertTrue(trackedRace.getStartOfTracking().before(startMarkPassingTimePoint));
        assertTrue(trackedRace.getStartOfTracking().after(new MillisecondsTimePoint(123456)));

        // test inference from finished time change by new blue flag down event
        newStartAndEndOfTrackingNotifiedByRace[0] = null;
        newStartAndEndOfTrackingNotifiedByRace[1] = null;
        final TimePoint finishedTimePoint = MillisecondsTimePoint.now();
        raceLog.add(new RaceLogRaceStatusEventImpl(finishedTimePoint, finishedTimePoint, author, UUID.randomUUID(), 0, RaceLogRaceStatus.FINISHED));
        assertNotNull(newStartAndEndOfTrackingNotifiedByRace[1]);
        assertTrue(trackedRace.getEndOfTracking().after(finishedTimePoint));

        // verify that setting a start and finished time through the race log adjusts the start/end of tracking times
        newStartAndEndOfTrackingNotifiedByRace[0] = null;
        newStartAndEndOfTrackingNotifiedByRace[1] = null;
        setStartAndEndOfRaceInRaceLog(123456, 234567);
        assertNotNull(trackedRace.getStartOfTracking());
        assertNotNull(trackedRace.getEndOfTracking());
        assertTrue(trackedRace.getStartOfTracking().before(new MillisecondsTimePoint(123456)));
        assertTrue(trackedRace.getEndOfTracking().after(new MillisecondsTimePoint(234567)));
        
        newStartAndEndOfTrackingNotifiedByRace[0] = null;
        newStartAndEndOfTrackingNotifiedByRace[1] = null;
        setManualTrackingTimesOnTrackedRace(trackedRace, 1111, 2222); // wrong values; race log should take precedence when available
        // assert that the event listener was triggered
        assertEquals(new MillisecondsTimePoint(1111), newStartAndEndOfTrackingNotifiedByRace[0]);
        assertEquals(new MillisecondsTimePoint(2222), newStartAndEndOfTrackingNotifiedByRace[1]);
        // test values set immediately
        assertEquals(new MillisecondsTimePoint(1111), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(2222), trackedRace.getEndOfTracking());
        
        newStartAndEndOfTrackingNotifiedByRace[0] = null;
        newStartAndEndOfTrackingNotifiedByRace[1] = null;
        setStartAndEndOfTrackingInRaceLog(10000, 20000); // correct values, taking precedence
        // assert that the event listener was triggered
        assertEquals(new MillisecondsTimePoint(10000), newStartAndEndOfTrackingNotifiedByRace[0]);
        assertEquals(new MillisecondsTimePoint(20000), newStartAndEndOfTrackingNotifiedByRace[1]);
        // test inference via racelog
        assertEquals(new MillisecondsTimePoint(10000), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(20000), trackedRace.getEndOfTracking());
        
        // shouldn't change anymore when setting explicitly because race log takes precedence
        newStartAndEndOfTrackingNotifiedByRace[0] = null;
        newStartAndEndOfTrackingNotifiedByRace[1] = null;
        setManualTrackingTimesOnTrackedRace(trackedRace, 1111, 2222);
        assertNull(newStartAndEndOfTrackingNotifiedByRace[0]);
        assertNull(newStartAndEndOfTrackingNotifiedByRace[1]);
        assertEquals(new MillisecondsTimePoint(10000), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(20000), trackedRace.getEndOfTracking());
        
        // test inference when setting null in RaceLog; RaceLog should still take precedence with its null values
        newStartAndEndOfTrackingNotifiedByRace[0] = MillisecondsTimePoint.now();
        newStartAndEndOfTrackingNotifiedByRace[1] = MillisecondsTimePoint.now();
        raceLog.add(new RaceLogStartOfTrackingEventImpl(null, author, 0));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(null, author, 0));
        assertNull(trackedRace.getStartOfTracking());
        assertNull(trackedRace.getEndOfTracking());
        assertNull(newStartAndEndOfTrackingNotifiedByRace[0]);
        assertNull(newStartAndEndOfTrackingNotifiedByRace[1]);
    }    
    
    /**
     * tests notification of first start time update through race log; see bug 3660
     */
    @Test
    public void testStartAndEndOfTrackingTimeChangeNotificationForFirstUpdateThroughRaceLog() throws TransformationException,
            NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null, null,
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(1), author, new MillisecondsTimePoint(1), 0, mark));
        regattaLog.add(new RegattaLogDefineMarkEventImpl(new MillisecondsTimePoint(2), author, new MillisecondsTimePoint(1), 0, mark2));
        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] { new WaypointImpl(mark),
                new WaypointImpl(mark2) }));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] { comp, comp2 }));
        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()), boatClass, /* startDate */
                null, /* endDate */null, null, null, "a", null));
        final DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race,
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, store, 0, 0, 0, /*useMarkPassingCalculator*/ false,
                OneDesignRankingMetric::new, mock(RaceLogResolver.class));
        trackedRace.attachRaceLog(raceLog);
        final TimePoint[] oldAndNewStartTimeNotifiedByRace = new TimePoint[2];
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
                oldAndNewStartTimeNotifiedByRace[0] = oldStartOfRace;
                oldAndNewStartTimeNotifiedByRace[1] = newStartOfRace;
            }
        });
        assertNull(trackedRace.getStartOfRace());
        final TimePoint newStartOfRace = MillisecondsTimePoint.now();
        raceLog.add(new RaceLogStartTimeEventImpl(newStartOfRace, author, 0, newStartOfRace));
        assertNull(oldAndNewStartTimeNotifiedByRace[0]);
        assertEquals(newStartOfRace, oldAndNewStartTimeNotifiedByRace[1]);
        assertEquals(newStartOfRace, trackedRace.getStartOfRace());
    }    
    

    
    
    
    
    
    public void setStartAndEndOfTrackingInRaceLog(int startTime, int endTime) {
        MillisecondsTimePoint startOfTrackingInRacelog = new MillisecondsTimePoint(startTime);
        MillisecondsTimePoint endOfTrackingInRacelog = new MillisecondsTimePoint(endTime);
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTrackingInRacelog, author, 0));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTrackingInRacelog, author, 0));
    }
    
    public void setStartAndEndOfRaceInRaceLog(int startTime, int endTime) {
        MillisecondsTimePoint startTimeInRaceLog2 = new MillisecondsTimePoint(startTime);
        MillisecondsTimePoint endTimeInRaceLog2 = new MillisecondsTimePoint(endTime);
        raceLog.add(new RaceLogStartTimeEventImpl(startTimeInRaceLog2, author, 0, startTimeInRaceLog2));
        raceLog.add(new RaceLogRaceStatusEventImpl(endTimeInRaceLog2, endTimeInRaceLog2, author, UUID.randomUUID(), 0, RaceLogRaceStatus.FINISHED));
    }
    
    public void setManualTrackingTimesOnTrackedRace(DynamicTrackedRace trackedRace, int startTime, int endTime) {
        trackedRace.setStartOfTrackingReceived(new MillisecondsTimePoint(startTime));
        trackedRace.setEndOfTrackingReceived(new MillisecondsTimePoint(endTime));
    }
}
