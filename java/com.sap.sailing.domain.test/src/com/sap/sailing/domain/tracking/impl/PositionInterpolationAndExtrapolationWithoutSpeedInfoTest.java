package com.sap.sailing.domain.tracking.impl;

import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sse.common.Duration;

public class PositionInterpolationAndExtrapolationWithoutSpeedInfoTest extends PositionInterpolationAndExtrapolationTest<GPSFix> {
    @Before
    public void setUp() {
        super.setUp();
        track = new GPSFixTrackImpl<>(new Object(), /* millisecondsOverWhichToAverage */ 8000);
    }
    
    @Test
    public void testEmptyTrack() {
        assertNull(track.getEstimatedPosition(now, /* extrapolate */ true));
        assertNull(track.getEstimatedPosition(now, /* extrapolate */ false));
    }
    
    @Test
    public void testFixBeforeNow() {
        GPSFix fixBeforeNow = new GPSFixImpl(p1, now.minus(Duration.ONE_HOUR));
        track.add(fixBeforeNow);
        assertPos(p1, /* extrapolate */ false);
        assertPos(p1, /* extrapolate */ true);
    }

    @Test
    public void testFixAfterNow() {
        GPSFix fixAfterNow = new GPSFixImpl(p1, now.plus(Duration.ONE_HOUR));
        track.add(fixAfterNow);
        assertPos(p1, /* extrapolate */ false);
        assertPos(p1, /* extrapolate */ true);
    }
    
    @Test
    public void testExactMatch() {
        GPSFix fixNow = new GPSFixImpl(p1, now);
        track.add(fixNow);
        assertPos(p1, /* extrapolate */ false);
        assertPos(p1, /* extrapolate */ true);
    }
    
    @Test
    public void testInBetween() {
        GPSFix fixBeforeNow = new GPSFixImpl(p1, now.minus(Duration.ONE_HOUR));
        track.add(fixBeforeNow);
        GPSFix fixAfterNow = new GPSFixImpl(p2, now.plus(Duration.ONE_HOUR));
        track.add(fixAfterNow);
        Position middle = p1.translateGreatCircle(p1.getBearingGreatCircle(p2), p1.getDistance(p2).scale(0.5));
        assertPos(middle, /* extrapolate */ false);
        assertPos(middle, /* extrapolate */ true);
    }
}
