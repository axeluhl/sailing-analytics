package com.sap.sailing.domain.tracking.impl;

import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sse.common.Duration;

public class PositionInterpolationAndExtrapolationWithSpeedInfoTest extends PositionInterpolationAndExtrapolationTest<GPSFixMoving> {
    private SpeedWithBearing speed;

    @Before
    public void setUp() {
        super.setUp();
        track = new DynamicGPSFixMovingTrackImpl<Object>(new Object(), /* millisecondsOverWhichToAverage */ 8000);
        speed = new KnotSpeedWithBearingImpl(6 /* knots */, Bearing.NORTH);
    }
    
    @Test
    public void testEmptyTrack() {
        assertNull(track.getEstimatedPosition(now, /* extrapolate */ true));
        assertNull(track.getEstimatedPosition(now, /* extrapolate */ false));
    }
    
    @Test
    public void testFixBeforeNow() {
        final GPSFixMoving fixBeforeNow = new GPSFixMovingImpl(p1, now.minus(Duration.ONE_HOUR), speed);
        track.add(fixBeforeNow);
        assertPos(p1, /* extrapolate */ false);
        assertPos(p1.translateGreatCircle(speed.getBearing(), speed.travel(now.minus(Duration.ONE_HOUR), now)), /* extrapolate */ true);
    }

    @Test
    public void testFixAfterNow() {
        GPSFixMoving fixAfterNow = new GPSFixMovingImpl(p1, now.plus(Duration.ONE_HOUR), speed);
        track.add(fixAfterNow);
        assertPos(p1, /* extrapolate */ false);
        assertPos(p1.translateGreatCircle(speed.getBearing().reverse() /* travel backwards */, speed.travel(now.minus(Duration.ONE_HOUR), now)), /* extrapolate */ true);
    }
    
    @Test
    public void testExactMatch() {
        GPSFixMoving fixNow = new GPSFixMovingImpl(p1, now, speed);
        track.add(fixNow);
        assertPos(p1, /* extrapolate */ false);
        assertPos(p1, /* extrapolate */ true);
    }

    @Test
    public void testInBetween() {
        GPSFixMoving fixBeforeNow = new GPSFixMovingImpl(p1, now.minus(Duration.ONE_HOUR), speed);
        track.add(fixBeforeNow);
        GPSFixMoving fixAfterNow = new GPSFixMovingImpl(p2, now.plus(Duration.ONE_HOUR), speed);
        track.add(fixAfterNow);
        Position middle = p1.translateGreatCircle(p1.getBearingGreatCircle(p2), p1.getDistance(p2).scale(0.5));
        assertPos(middle, /* extrapolate */ false);
        assertPos(middle, /* extrapolate */ true);
    }
}
