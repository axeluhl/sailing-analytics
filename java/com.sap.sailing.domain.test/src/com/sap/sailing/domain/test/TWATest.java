package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;
import com.sap.sailing.domain.tracking.impl.TrackedLegOfCompetitorImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TWATest {
    private Competitor competitor;
    private GPSFixTrack<Competitor, GPSFixMoving> competitorTrack;
    private TrackedRace trackedRace;
    private TrackedLeg trackedLeg;
    private TrackedLegOfCompetitor trackedLegOfCompetitor;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        competitor = mock(CompetitorImpl.class);
        trackedRace = mock(DynamicTrackedRaceImpl.class);
        trackedLeg = mock(TrackedLegImpl.class);
        trackedLegOfCompetitor = mock(TrackedLegOfCompetitorImpl.class);
        competitorTrack = mock(DynamicGPSFixMovingTrackImpl.class);
        when(competitorTrack.getEstimatedPosition((TimePoint) anyObject(), /* extrapolate */ anyBoolean())).thenReturn(new DegreePosition(0, 0));
        when(trackedLegOfCompetitor.getCompetitor()).thenReturn(competitor);
        when(trackedLegOfCompetitor.getTrackedLeg()).thenReturn(trackedLeg);
        when(trackedRace.getTrack(competitor)).thenReturn(competitorTrack);
        when(trackedLeg.getTrackedRace()).thenReturn(trackedRace);
    }
    
    private void setUp(final Bearing windToBearing, final Bearing boatToBearing, final Bearing legBearing, LegType legType)
            throws NoWindException {
        when(trackedLeg.getLegBearing((TimePoint) anyObject())).thenReturn(legBearing);
        when(trackedLeg.getLegType((TimePoint) anyObject())).thenReturn(legType);
        when(trackedRace.getWind((Position) anyObject(), (TimePoint) anyObject())).thenReturn(
                new WindImpl(new DegreePosition(0, 0), MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                        windToBearing)));
        
        when(competitorTrack.getEstimatedSpeed((TimePoint) anyObject())).thenReturn(new KnotSpeedWithBearingImpl(10, boatToBearing));
        when(trackedRace.getTWA(Matchers.eq(competitor), (TimePoint) anyObject())).thenCallRealMethod();
        when(trackedRace.getTWA(Matchers.eq(competitor), (TimePoint) anyObject(), (WindLegTypeAndLegBearingCache) anyObject())).thenCallRealMethod();
    }

    @Test
    public void testTWAUpwindPort() throws NoWindException {
        final Bearing windToBearing = new DegreeBearingImpl(123);
        final Bearing boatToBearing = new DegreeBearingImpl(348);
        final Bearing legBearing = new DegreeBearingImpl(303);
        setUp(windToBearing, boatToBearing, legBearing, LegType.UPWIND);
        assertEquals(-45., trackedRace.getTWA(competitor, MillisecondsTimePoint.now()).getDegrees(), 0.00001);
    }

    @Test
    public void testTWAUpwindStarboard() throws NoWindException {
        final Bearing windToBearing = new DegreeBearingImpl(123);
        final Bearing boatToBearing = new DegreeBearingImpl(258);
        final Bearing legBearing = new DegreeBearingImpl(303);
        setUp(windToBearing, boatToBearing, legBearing, LegType.UPWIND);
        assertEquals(45., trackedRace.getTWA(competitor, MillisecondsTimePoint.now()).getDegrees(), 0.00001);
    }

    @Test
    public void testTWADownwindPort() throws NoWindException {
        final Bearing windToBearing = new DegreeBearingImpl(123);
        final Bearing boatToBearing = new DegreeBearingImpl(120);
        final Bearing legBearing = new DegreeBearingImpl(123);
        setUp(windToBearing, boatToBearing, legBearing, LegType.DOWNWIND);
        assertEquals(-177., trackedRace.getTWA(competitor, MillisecondsTimePoint.now()).getDegrees(), 0.00001);
    }

    @Test
    public void testTWADownwindStarboard() throws NoWindException {
        final Bearing windToBearing = new DegreeBearingImpl(123);
        final Bearing boatToBearing = new DegreeBearingImpl(126);
        final Bearing legBearing = new DegreeBearingImpl(123);
        setUp(windToBearing, boatToBearing, legBearing, LegType.DOWNWIND);
        assertEquals(177., trackedRace.getTWA(competitor, MillisecondsTimePoint.now()).getDegrees(), 0.00001);
    }

    @Test
    public void testTWAReachingPort() throws NoWindException {
        final Bearing windToBearing = new DegreeBearingImpl(123);
        final Bearing boatToBearing = new DegreeBearingImpl(33);
        final Bearing legBearing = new DegreeBearingImpl(33);
        setUp(windToBearing, boatToBearing, legBearing, LegType.REACHING);
        assertEquals(-90., trackedRace.getTWA(competitor, MillisecondsTimePoint.now()).getDegrees(), 0.00001);
    }

    @Test
    public void testTWAReachingStarboard() throws NoWindException {
        final Bearing windToBearing = new DegreeBearingImpl(303);
        final Bearing boatToBearing = new DegreeBearingImpl(33);
        final Bearing legBearing = new DegreeBearingImpl(33);
        setUp(windToBearing, boatToBearing, legBearing, LegType.REACHING);
        assertEquals(90., trackedRace.getTWA(competitor, MillisecondsTimePoint.now()).getDegrees(), 0.00001);
    }
}
