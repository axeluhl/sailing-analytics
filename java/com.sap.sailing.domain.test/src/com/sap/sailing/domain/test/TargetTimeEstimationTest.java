package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TargetTimeEstimationTest {
    
    @Test
    public void simpleReachTargetTimeEstimation() throws NotEnoughDataHasBeenAddedException, NoWindException {
        // Setup mock objects
        PolarDataService mockedPolars = mock(PolarDataService.class);
        BoatClass mockedBoatClass = mock(BoatClass.class);
        Position centerOfCourse = new DegreePosition(54.432800, 10.193655);
        TimePoint timepoint = new MillisecondsTimePoint(1431426491696l);
        Bearing windBearing = new DegreeBearingImpl(90);
        SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(10, windBearing);
        Wind wind = new WindImpl(centerOfCourse, timepoint, windSpeedWithBearing);
        Position startOfLeg = new DegreePosition(54.434648, 10.193312);
        Position endOfLeg = new DegreePosition(54.430454, 10.193226);
        Bearing legBearing = startOfLeg.getBearingGreatCircle(endOfLeg);
        SpeedWithConfidence<Void> boatSpeedWithConfidence = new SpeedWithConfidenceImpl<Void>(new KnotSpeedImpl(12), 1, null);
        DynamicTrackedRaceImpl trackedRace = mock(DynamicTrackedRaceImpl.class);
        when(trackedRace.getCenterOfCourse(timepoint)).thenReturn(centerOfCourse);
        WindSource source = mock(WindSource.class);
        Set<WindSource> sources = Collections.singleton(source);
        when(trackedRace.getWindSources(WindSourceType.TRACK_BASED_ESTIMATION)).thenReturn(sources);
        when(trackedRace.getWind(any(Position.class), eq(timepoint), eq(sources))).thenReturn(wind);
        
        RaceDefinition race = mock(RaceDefinition.class);
        when(race.getBoatClass()).thenReturn(mockedBoatClass);
        
        when(trackedRace.getRace()).thenReturn(race);
        
        Leg leg = mock(Leg.class);
        Waypoint from = mock(Waypoint.class);
        Waypoint to = mock(Waypoint.class);
        when(leg.getFrom()).thenReturn(from);
        when(leg.getTo()).thenReturn(to);
        
        when(trackedRace.getApproximatePosition(from, timepoint)).thenReturn(startOfLeg);
        when(trackedRace.getApproximatePosition(to, timepoint)).thenReturn(endOfLeg);
        
        when(mockedPolars.getSpeed(mockedBoatClass, wind, windBearing.getDifferenceTo(legBearing))).thenReturn(boatSpeedWithConfidence);
        
        HashSet<Competitor> competitors = new HashSet<Competitor>();
        TrackedLeg trackedLeg = new TrackedLegImpl(trackedRace, leg, competitors);
        
        Duration duration = trackedLeg.getEstimatedTimeToComplete(mockedPolars, timepoint);
        assertEquals(75494, duration.asMillis(), 100);
        
    }

}
