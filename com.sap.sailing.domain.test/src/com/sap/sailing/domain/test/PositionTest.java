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
        assertTrue(p1.getDistance(p2).getSeaMiles() < 10);
        Position northPole = new DegreePosition(90, 0);
        Position southPole = new DegreePosition(-90, 0);
        assertEquals(20004, Math.abs(southPole.getDistance(northPole).getKilometers()), 0.0001);
    }
    
    @Test
    public void bearingTest() {
        // Note that the bearing is a bearing on a great circle!
        Position p1 = new DegreePosition(49.2, 008.3);
        Position p2 = new DegreePosition(49.3, 008.3);
        assertEquals(0.0, p1.getBearingGreatCircle(p2).getDegrees(), 0.1);
        
        Position p3 = new DegreePosition(49.2, 008.3);
        Position p4 = new DegreePosition(49.2, 008.4);
        assertEquals(90.0, p3.getBearingGreatCircle(p4).getDegrees(), 0.1);

        Position p5 = new DegreePosition(0.0, 008.3);
        Position p6 = new DegreePosition(0.1, 008.4);
        Position p7 = new DegreePosition(0.1, 008.2);
        assertEquals(45.0, p5.getBearingGreatCircle(p6).getDegrees(), 0.1);
        assertEquals(315.0, p5.getBearingGreatCircle(p7).getDegrees(), 0.1);
        assertEquals(135.0, p7.getBearingGreatCircle(p5).getDegrees(), 0.1);
        assertEquals(225.0, p6.getBearingGreatCircle(p5).getDegrees(), 0.1);
    }
    
    @Test
    public void testTranslateGreatCircle() {
        Position p1 = new DegreePosition(49.2, 008.3);
        Position p2 = new DegreePosition(49.3, 008.3);
        assertEquals(0, p1.translateGreatCircle(p1.getBearingGreatCircle(p2), p1.getDistance(p2)).getDistance(p2).getMeters(), 0.00001);
    }
    
}
