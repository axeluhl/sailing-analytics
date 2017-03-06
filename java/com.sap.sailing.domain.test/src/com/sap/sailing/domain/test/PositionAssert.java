package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;

public class PositionAssert {
    public static void assertPositionEquals(Position p1, Position p2, double degreeDelta) {
        assertEquals(p1.getLatDeg(), p2.getLatDeg(), degreeDelta);
        assertEquals(p1.getLngDeg(), p2.getLngDeg(), degreeDelta);
    }

    public static void assertGPSFixEquals(GPSFixMoving f1, GPSFixMoving f2, double positionDegreeDelta, double bearingDegreeDelta, double knotDelta) {
        assertGPSFixEquals((GPSFix) f1, (GPSFix) f2, positionDegreeDelta);
        assertBearingEquals(f1.getSpeed().getBearing(), f2.getSpeed().getBearing(), bearingDegreeDelta);
        assertEquals(f1.getSpeed().getKnots(), f2.getSpeed().getKnots(), knotDelta);
    }

    public static void assertBearingEquals(Bearing b1, Bearing b2, double bearingDegreeDelta) {
        assertEquals(b1.getDegrees(), b2.getDegrees(), bearingDegreeDelta);
    }

    public static void assertGPSFixEquals(GPSFix f1, GPSFix f2, double positionDegreeDelta) {
        assertPositionEquals(f1.getPosition(), f2.getPosition(), positionDegreeDelta);
        assertEquals(f1.getTimePoint(), f2.getTimePoint());
    }
}
