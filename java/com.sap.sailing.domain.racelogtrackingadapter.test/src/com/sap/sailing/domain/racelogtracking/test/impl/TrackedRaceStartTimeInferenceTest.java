package com.sap.sailing.domain.racelogtracking.test.impl;

import static org.junit.Assert.assertEquals;
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
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackedRaceStartTimeInferenceTest extends AbstractGPSFixStoreTest {
    private final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");

    /**
     * tests the precedence order described in {@link TrackedRaceImpl#updateStartAndEndOfTracking()}
     */
    @Test
    public void testStartTimeInferencePrecedenceOrder() throws TransformationException,
            NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null, null, null,
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null);
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
        
        //test inference via race start/end time in racelog
        trackedRace.waitForLoadingFromGPSFixStoreToFinishRunning(regattaLog);
        MillisecondsTimePoint startOfRaceInRaceLog = new MillisecondsTimePoint(10000);
        TimePoint expectedStartOfTracking = startOfRaceInRaceLog.minus(Duration.ONE_MINUTE.times(TrackedRaceImpl.TRACKING_BUFFER_IN_MINUTES));
        MillisecondsTimePoint endOfRaceInRaceLog = new MillisecondsTimePoint(20000);
        TimePoint expectedEndOfTracking = endOfRaceInRaceLog.plus(Duration.ONE_MINUTE.times(TrackedRaceImpl.TRACKING_BUFFER_IN_MINUTES));
        assertEquals(expectedStartOfTracking, trackedRace.getStartOfTracking());
        assertEquals(expectedEndOfTracking, trackedRace.getEndOfTracking());
        
        
        //test inference via manually set start/end of tracking
        setManualTrackingTimesOnTrackedRace(trackedRace, 30000, 40000);
        assertEquals(new MillisecondsTimePoint(30000), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(40000), trackedRace.getEndOfTracking());
        
        //shouldn't change when setting time in raceLog again
        setStartAndEndOfRaceInRaceLog(100000, 200000);
        assertEquals(new MillisecondsTimePoint(30000), trackedRace.getStartOfTracking());
        assertEquals(new MillisecondsTimePoint(40000), trackedRace.getEndOfTracking());
        
        //test inference via start/end of tracking in RaceLog
        setStartAndEndOfTrackingInRaceLog(50000, 60000);
        MillisecondsTimePoint startOfTrackingInRacelog = new MillisecondsTimePoint(50000);
        MillisecondsTimePoint endOfTrackingInRacelog = new MillisecondsTimePoint(60000);
        assertEquals(startOfTrackingInRacelog, trackedRace.getStartOfTracking());
        assertEquals(endOfTrackingInRacelog, trackedRace.getEndOfTracking());
        
        //shouldn't change when setting time in raceLog again
        setStartAndEndOfRaceInRaceLog(300000, 400000);
        assertEquals(startOfTrackingInRacelog, trackedRace.getStartOfTracking());
        assertEquals(endOfTrackingInRacelog, trackedRace.getEndOfTracking());
        
        //shouldn't change when setting start/end of tracking
        setManualTrackingTimesOnTrackedRace(trackedRace, 500000, 600000);
        assertEquals(startOfTrackingInRacelog, trackedRace.getStartOfTracking());
        assertEquals(endOfTrackingInRacelog, trackedRace.getEndOfTracking());
    }    
    
    
    public void setStartAndEndOfTrackingInRaceLog(int startTime, int endTime){
        MillisecondsTimePoint startOfTrackingInRacelog = new MillisecondsTimePoint(startTime);
        MillisecondsTimePoint endOfTrackingInRacelog = new MillisecondsTimePoint(endTime);
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTrackingInRacelog, author, 0));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTrackingInRacelog, author, 0));
    }
    
    public void setStartAndEndOfRaceInRaceLog(int startTime, int endTime){
        MillisecondsTimePoint startTimeInRaceLog2 = new MillisecondsTimePoint(startTime);
        MillisecondsTimePoint endTimeInRaceLog2 = new MillisecondsTimePoint(endTime);
        raceLog.add(new RaceLogStartTimeEventImpl(startTimeInRaceLog2, author, 0, startTimeInRaceLog2));
        raceLog.add(new RaceLogRaceStatusEventImpl(endTimeInRaceLog2, endTimeInRaceLog2, author, UUID.randomUUID(), 0, RaceLogRaceStatus.FINISHED));
    }
    
    public void setManualTrackingTimesOnTrackedRace(DynamicTrackedRace trackedRace, int startTime, int endTime){
        trackedRace.setStartOfTrackingReceived(new MillisecondsTimePoint(startTime));
        trackedRace.setEndOfTrackingReceived(new MillisecondsTimePoint(endTime));
    }
}
