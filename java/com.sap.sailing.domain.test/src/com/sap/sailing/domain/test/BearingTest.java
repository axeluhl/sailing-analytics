package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.RadianBearingImpl;
import com.sap.sailing.domain.common.Bearing;

public class BearingTest {
    @Test
    public void testDegreeBearingsGreaterThan360Deg() {
        Bearing bearing = new DegreeBearingImpl(355);
        assertEquals(355, bearing.getDegrees(), 0.000000001);
        Bearing bearing2 = new DegreeBearingImpl(365);
        assertEquals(5, bearing2.getDegrees(), 0.000000001);
    }

    @Test
    public void testRadianBearingsGreaterThan360Deg() {
        Bearing bearing = new RadianBearingImpl(355./180.*Math.PI);
        assertEquals(355, bearing.getDegrees(), 0.000000001);
        Bearing bearing2 = new RadianBearingImpl(365./180.*Math.PI);
        assertEquals(5, bearing2.getDegrees(), 0.000000001);
    }
}
