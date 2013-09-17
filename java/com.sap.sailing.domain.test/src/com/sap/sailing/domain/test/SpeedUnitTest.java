package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;

public class SpeedUnitTest {
    @Test
    public void testMeterPerSecondSpeedToKnots() {
        Speed s = new MeterPerSecondSpeedImpl(1852./3600.);
        assertEquals(1, s.getKnots(), 0.0000001);
    }
}
