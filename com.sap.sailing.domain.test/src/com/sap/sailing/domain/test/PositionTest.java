package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.impl.DegreePosition;

public class PositionTest {
    @Test
    public void distanceTest() {
        Position p1 = new DegreePosition(49.2, 008.3);
        Position p2 = new DegreePosition(49.3, 008.2);
        assertTrue(p1.distance(p2).getSeaMiles() < 10);
        Position northPole = new DegreePosition(90, 0);
        Position southPole = new DegreePosition(-90, 0);
        assertEquals(20004, Math.abs(southPole.distance(northPole).getKilometers()), 0.0001);
    }
    
    @Test
    public void bearingTest() {
        Position p1 = new DegreePosition(49.2, 008.3);
        Position p2 = new DegreePosition(49.3, 008.3);
        assertEquals(0.0, p1.getBearingDeg(p2), 0.1);
        
        Position p3 = new DegreePosition(49.2, 008.3);
        Position p4 = new DegreePosition(49.2, 008.4);
        assertEquals(90.0, p3.getBearingDeg(p4), 0.1);

        Position p5 = new DegreePosition(0.0, 008.3);
        Position p6 = new DegreePosition(0.1, 008.4);
        Position p7 = new DegreePosition(0.1, 008.2);
        assertEquals(45.0, p5.getBearingDeg(p6), 0.1);
        assertEquals(315.0, p5.getBearingDeg(p7), 0.1);
        assertEquals(135.0, p7.getBearingDeg(p5), 0.1);
        assertEquals(225.0, p6.getBearingDeg(p5), 0.1);
    }
    
}
