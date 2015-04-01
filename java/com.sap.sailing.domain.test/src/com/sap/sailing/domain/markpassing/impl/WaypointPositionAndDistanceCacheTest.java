package com.sap.sailing.domain.markpassing.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.markpassingcalculation.impl.WaypointPositionAndDistanceCache;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WaypointPositionAndDistanceCacheTest {
    private WaypointPositionAndDistanceCache cache;
    private int recalculations;
    private DynamicTrackedRace trackedRace;
    private TimePoint now;
    private Waypoint start;
    private Waypoint windwardWaypoint;
    private Waypoint finish;
    
    @Before
    public void setUp() {
        final RaceDefinition race = mock(RaceDefinition.class);
        when(race.getCompetitors()).thenReturn(Collections.emptyList());
        when(race.getBoatClass()).thenReturn(DomainFactory.INSTANCE.getOrCreateBoatClass("29er"));
        Mark pinEnd = new MarkImpl("Pin End");
        Mark startBoat = new MarkImpl("Start Boat");
        Mark windward = new MarkImpl("Windward");
        ControlPointWithTwoMarks startFinish = new ControlPointWithTwoMarksImpl(UUID.randomUUID(), pinEnd, startBoat, "Start/Finish");
        start = new WaypointImpl(startFinish);
        windwardWaypoint = new WaypointImpl(windward);
        finish = new WaypointImpl(startFinish);
        Course course = new CourseImpl("Course name", Arrays.asList(start, windwardWaypoint, finish));
        when(race.getCourse()).thenReturn(course);
        final TrackedRegatta trackedRegatta = mock(TrackedRegatta.class);
        final Regatta regatta = mock(Regatta.class);
        when(regatta.getName()).thenReturn("Regatta name");
        when(trackedRegatta.getRegatta()).thenReturn(regatta);
        trackedRace = new DynamicTrackedRaceImpl(trackedRegatta, race, Collections.emptyList(), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE,
                /* delayToLiveInMillis */ 8000, /* millisecondsOverWhichToAverageWind */ 30000,
                /* millisecondsOverWhichToAverageSpeed */ 15000, /* delayForCacheInvalidationOfWindEstimation */ 10000,
                /* useInternalMarkPassingAlgorithm */ false);
        now = MillisecondsTimePoint.now();
        trackedRace.getOrCreateTrack(pinEnd).addGPSFix(new GPSFixImpl(new DegreePosition(0, -0.0000001), now));
        trackedRace.getOrCreateTrack(startBoat).addGPSFix(new GPSFixImpl(new DegreePosition(0, 0.0000001), now));
        trackedRace.getOrCreateTrack(windward).addGPSFix(new GPSFixImpl(new DegreePosition(1, 0), now));
        recalculations = 0;
        cache = new WaypointPositionAndDistanceCache(trackedRace, /* timeRangeResolution */ Duration.ONE_MINUTE) {
            @Override
            protected <R> R computeResult(Function<TimePoint, R> resultCalculator, TimePoint roundedToTimeRangeCenter) {
                recalculations++;
                return super.computeResult(resultCalculator, roundedToTimeRangeCenter);
            }
        };
    }
    
    @Test
    public void simpleTest() {
        assertEquals(60, cache.getApproximateDistance(start, windwardWaypoint, now).getNauticalMiles(), 0.01);
        assertEquals(3, recalculations); // one for the position of each waypoint, one for the distance
    }

    @Test
    public void testBackAndForth() {
        assertEquals(60, cache.getApproximateDistance(start, windwardWaypoint, now).getNauticalMiles(), 0.01);
        assertEquals(60, cache.getApproximateDistance(windwardWaypoint, start, now).getNauticalMiles(), 0.01);
        assertEquals(3, recalculations); // one for the position of each waypoint, one for the distance which creates two symmetrical cache entries
    }
}
