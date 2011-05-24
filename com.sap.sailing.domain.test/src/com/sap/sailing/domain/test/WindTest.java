package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class WindTest {
    private static final int AVERAGING_INTERVAL_MILLIS = 30000 /* 30s averaging interval */;

    @Test
    public void testSingleElementWindTrack() {
        WindTrack track = new WindTrackImpl(AVERAGING_INTERVAL_MILLIS);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(0));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }

    @Test
    public void testSingleElementExtrapolation() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        // we only have one measurement; this should be extrapolated because it's our best guess
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(1000));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }

    @Test
    public void testSingleElementExtrapolationBeyondThreshold() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedImpl(10, new DegreeBearingImpl(123)));
        track.add(wind);
        // we only have one measurement; this should be extrapolated because it's our best guess even if
        // the last measurement was longer ago than our smoothening interval
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(AVERAGING_INTERVAL_MILLIS+1000));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(123, estimate.getBearing().getDegrees(), 0.000000001);
    }

    @Test
    public void testTwoElementWindTrackSameBearing() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind1 = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedImpl(10, new DegreeBearingImpl(100)));
        track.add(wind1);
        Wind wind2 = new WindImpl(pos, new MillisecondsTimePoint(1000), new KnotSpeedImpl(20, new DegreeBearingImpl(100)));
        track.add(wind2);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(2000));
        assertEquals(15, estimate.getKnots(), 0.000000001);
        assertEquals(100, estimate.getBearing().getDegrees(), 0.00000001);
    }

    @Test
    public void testTwoElementWindTrackDifferentBearing() {
        WindTrack track = new WindTrackImpl(30000 /* 30s averaging interval */);
        DegreePosition pos = new DegreePosition(0, 0);
        Wind wind1 = new WindImpl(pos, new MillisecondsTimePoint(0), new KnotSpeedImpl(10, new DegreeBearingImpl(110)));
        track.add(wind1);
        Wind wind2 = new WindImpl(pos, new MillisecondsTimePoint(1000), new KnotSpeedImpl(10, new DegreeBearingImpl(100)));
        track.add(wind2);
        Wind estimate = track.getEstimatedWind(pos, new MillisecondsTimePoint(2000));
        assertEquals(10, estimate.getKnots(), 0.000000001);
        assertEquals(105, estimate.getBearing().getDegrees(), 0.00000001);
    }
}
