package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.util.Util;

public class CourseTest {
    @Test
    public void testEmptyCourse() {
        Iterable<Waypoint> waypoints = Collections.emptyList();
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(0, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }

    @Test
    public void testCourseWithOneWaypoint() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        waypoints.add(new WaypointImpl(new BuoyImpl("Test Buoy")));
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }
    
    @Test
    public void testAddWaypointToCourseWithOneWaypoint() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        waypoints.add(new WaypointImpl(new BuoyImpl("Test Buoy")));
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        course.addWaypoint(1, new WaypointImpl(new BuoyImpl("Second Buoy")));
        assertEquals(2, Util.size(course.getWaypoints()));
        assertEquals(1, Util.size(course.getLegs()));
    }

    @Test
    public void testAddWaypointToEmptyCourse() {
        Iterable<Waypoint> waypoints = Collections.emptyList();
        Course course = new CourseImpl("Test Course", waypoints);
        assertEquals(0, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        course.addWaypoint(0, new WaypointImpl(new BuoyImpl("First Buoy")));
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }

    @Test
    public void testRemoveWaypointToCourseWithOneWaypoint() {
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        waypoints.add(new WaypointImpl(new BuoyImpl("Test Buoy")));
        waypoints.add(new WaypointImpl(new BuoyImpl("Second Buoy")));
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
        course.addWaypoint(0, new WaypointImpl(new BuoyImpl("First Buoy")));
        assertEquals(1, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
        course.removeWaypoint(0);
        assertEquals(0, Util.size(course.getWaypoints()));
        assertEquals(0, Util.size(course.getLegs()));
    }
}
