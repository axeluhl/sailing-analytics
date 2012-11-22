package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.impl.Util;

public class CourseTest {
    @Test
    public void testEmptyCourse() {
        Iterable<Waypoint> waypoints = Collections.emptyList();
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(0, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        assertNull(course.getFirstWaypoint());
        assertNull(course.getLastWaypoint());
    }

    @Test
    public void testCourseWithOneWaypoint() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        waypoints.add(new WaypointImpl(new MarkImpl("Test Mark")));
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }
    
    @Test
    public void testAddWaypointToCourseWithOneWaypoint() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        waypoints.add(new WaypointImpl(new MarkImpl("Test Mark")));
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        course.addWaypoint(1, new WaypointImpl(new MarkImpl("Second Mark")));
        assertEquals(2, Util.size(course.getWaypoints()));
        assertEquals(1, Util.size(course.getLegs()));
    }

    @Test
    public void testAddWaypointToEmptyCourse() {
        Iterable<Waypoint> waypoints = Collections.emptyList();
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(0, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        course.addWaypoint(0, new WaypointImpl(new MarkImpl("First Mark")));
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }

    @Test
    public void testRemoveWaypointToCourseWithOneWaypoint() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        waypoints.add(new WaypointImpl(new MarkImpl("Test Mark")));
        waypoints.add(new WaypointImpl(new MarkImpl("Second Mark")));
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(2, Util.size(course.getWaypoints()));
        assertEquals(1, Util.size(course.getLegs()));
        course.removeWaypoint(1);
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }

    @Test
    public void testRemoveWaypointToEmptyCourse() {
        Iterable<Waypoint> waypoints = Collections.emptyList();
        Course course = new CourseImpl("Test Course", waypoints);
        course.addWaypoint(0, new WaypointImpl(new MarkImpl("First Mark")));
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        course.removeWaypoint(0);
        assertEquals(0, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }

    @Test
    public void testInsertWaypointToCourseWithTwoWaypoints() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        final WaypointImpl wp1 = new WaypointImpl(new MarkImpl("Test Mark 1"));
        waypoints.add(wp1);
        final WaypointImpl wp2 = new WaypointImpl(new MarkImpl("Test Mark 2"));
        waypoints.add(wp2);
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(2, Util.size(course.getWaypoints()));
        assertEquals(1, Util.size(course.getLegs()));
        final WaypointImpl wp1_5 = new WaypointImpl(new MarkImpl("Test Mark 1.5"));
        course.addWaypoint(1, wp1_5);
        assertEquals(3, Util.size(course.getWaypoints()));
        assertEquals(2, Util.size(course.getLegs()));
        assertTrue(Util.equals(Arrays.asList(new Waypoint[] { wp1, wp1_5, wp2 }), course.getWaypoints()));
        assertEquals(0, course.getIndexOfWaypoint(wp1));
        assertEquals(1, course.getIndexOfWaypoint(wp1_5));
        assertEquals(2, course.getIndexOfWaypoint(wp2));
    }

    @Test
    public void testRemovetWaypointFromCourseWithThreeWaypoints() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        final WaypointImpl wp1 = new WaypointImpl(new MarkImpl("Test Mark 1"));
        waypoints.add(wp1);
        final WaypointImpl wp2 = new WaypointImpl(new MarkImpl("Test Mark 2"));
        waypoints.add(wp2);
        final WaypointImpl wp3 = new WaypointImpl(new MarkImpl("Test Mark 3"));
        waypoints.add(wp3);
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(3, Util.size(course.getWaypoints()));
        assertEquals(2, Util.size(course.getLegs()));
        course.removeWaypoint(1);
        assertEquals(2, Util.size(course.getWaypoints()));
        assertEquals(1, Util.size(course.getLegs()));
        assertTrue(Util.equals(Arrays.asList(new Waypoint[] { wp1, wp3 }), course.getWaypoints()));
        assertEquals(0, course.getIndexOfWaypoint(wp1));
        assertEquals(-1, course.getIndexOfWaypoint(wp2));
        assertEquals(1, course.getIndexOfWaypoint(wp3));
    }

}
