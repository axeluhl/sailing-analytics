package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WindRangeCalculationTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    public void testWithinRangeCalculation() {
        WindRangeForManeuverNode range = new WindRangeForManeuverNode(20, 10);
        assertEquals(range.isWindCourseWithinRange(20), true);
        assertEquals(range.isWindCourseWithinRange(25), true);
        assertEquals(range.isWindCourseWithinRange(30), true);
        assertEquals(range.isWindCourseWithinRange(19), false);
        assertEquals(range.isWindCourseWithinRange(200), false);
        assertEquals(range.isWindCourseWithinRange(31), false);

        range = new WindRangeForManeuverNode(350, 20);
        assertEquals(range.isWindCourseWithinRange(350), true);
        assertEquals(range.isWindCourseWithinRange(0), true);
        assertEquals(range.isWindCourseWithinRange(10), true);
        assertEquals(range.isWindCourseWithinRange(349), false);
        assertEquals(range.isWindCourseWithinRange(180), false);
        assertEquals(range.isWindCourseWithinRange(11), false);

        range = new WindRangeForManeuverNode(340, 20);
        assertEquals(range.isWindCourseWithinRange(0), true);
        assertEquals(range.isWindCourseWithinRange(1), false);
        assertEquals(range.isWindCourseWithinRange(339), false);
        assertEquals(range.isWindCourseWithinRange(180), false);

        range = new WindRangeForManeuverNode(340, 350);
        assertEquals(range.isWindCourseWithinRange(341), true);
        assertEquals(range.isWindCourseWithinRange(330), true);
        assertEquals(range.isWindCourseWithinRange(331), false);
        assertEquals(range.isWindCourseWithinRange(339), false);
    }

    @Test
    public void testWindRangeIntersectionCalculation() {
        WindRangeForManeuverNode range = new WindRangeForManeuverNode(20, 15);
        WindRangeForManeuverNode other = new WindRangeForManeuverNode(10, 20);
        IntersectedWindRange intersect = range.intersect(other);
        assertEquals(intersect.getFromPortside(), 20, TOLERANCE);
        assertEquals(intersect.getAngleTowardStarboard(), 10, TOLERANCE);
        assertEquals(intersect.getViolationRange(), 0, TOLERANCE);
        IntersectedWindRange intersect2 = other.intersect(range);
        assertEquals(intersect, intersect2);

        range = new WindRangeForManeuverNode(340, 15);
        other = new WindRangeForManeuverNode(350, 20);
        intersect = range.intersect(other);
        assertEquals(intersect.getFromPortside(), 350, TOLERANCE);
        assertEquals(intersect.getAngleTowardStarboard(), 5, TOLERANCE);
        assertEquals(intersect.getViolationRange(), 0, TOLERANCE);
        intersect2 = other.intersect(range);
        assertEquals(intersect, intersect2);

        range = new WindRangeForManeuverNode(340, 30);
        other = new WindRangeForManeuverNode(10, 20);
        intersect = range.intersect(other);
        assertEquals(intersect.getFromPortside(), 10, TOLERANCE);
        assertEquals(intersect.getAngleTowardStarboard(), 0, TOLERANCE);
        assertEquals(intersect.getViolationRange(), 0, TOLERANCE);
        intersect2 = other.intersect(range);
        assertEquals(intersect, intersect2);

        range = new WindRangeForManeuverNode(340, 30);
        other = new WindRangeForManeuverNode(330, 20);
        intersect = range.intersect(other);
        assertEquals(intersect.getFromPortside(), 340, TOLERANCE);
        assertEquals(intersect.getAngleTowardStarboard(), 10, TOLERANCE);
        assertEquals(intersect.getViolationRange(), 0, TOLERANCE);
        intersect2 = other.intersect(range);
        assertEquals(intersect, intersect2);
    }

    @Test
    public void testWindRangeIntersectionCalculationWithViolations() {
        WindRangeForManeuverNode range = new WindRangeForManeuverNode(20, 15);
        WindRangeForManeuverNode other = new WindRangeForManeuverNode(10, 4);
        IntersectedWindRange intersect = range.intersect(other);
        assertEquals(intersect.getFromPortside(), 10, TOLERANCE);
        assertEquals(intersect.getAngleTowardStarboard(), 4, TOLERANCE);
        assertEquals(intersect.getViolationRange(), 6, TOLERANCE);
        IntersectedWindRange intersect2 = other.intersect(range);
        assertEquals(new IntersectedWindRange(range.getFromPortside(), range.getAngleTowardStarboard(),
                intersect.getViolationRange()), intersect2);
    }
    
    @Test
    public void testWindRangeInversion() {
        WindRangeForManeuverNode range = new WindRangeForManeuverNode(10, 15);
        WindRangeForManeuverNode invert = range.invert();
        assertEquals(invert.getFromPortside(), 25, TOLERANCE);
        assertEquals(invert.getAngleTowardStarboard(), 345, TOLERANCE);
        assertEquals(invert.invert(), range);
    }

}
