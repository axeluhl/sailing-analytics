package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRaceAsWaypointList;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.Util;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

/**
 * Related to bug 2223; updates a course while serialization is in progress and validates that the
 * resulting TrackedRace's structures are eventually consistent with the modified course.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CourseUpdateDuringNonAtomicSerializationTest implements Serializable {
    private static final long serialVersionUID = -6207158826716014865L;
    private Course course;
    private Map<Integer, Waypoint> addCalls;
    private Map<Integer, Waypoint> removeCalls;
    private TrackedRaceImpl trackedRace;
    private TrackedRaceAsWaypointList trackedRaceWaypoints;
    
    public void setUp(int numberOfWaypoints) {
        final List<Waypoint> waypoints = new ArrayList<>();
        for (int i=0; i<numberOfWaypoints; i++) {
            waypoints.add(new WaypointImpl(new MarkImpl("Mark "+i)));
        }
        addCalls = new HashMap<>();
        removeCalls = new HashMap<>();
        course = new CourseImpl("Test Course", waypoints);
        TrackedRegatta trackedRegatta = mock(TrackedRegatta.class, withSettings().serializable());
        Regatta regatta = mock(Regatta.class, withSettings().serializable());
        when(trackedRegatta.getRegatta()).thenReturn(regatta);
        when(regatta.getName()).thenReturn("Test Regatta");
        RaceDefinition race = mock(RaceDefinition.class, withSettings().serializable());
        BoatClass boatClass = mock(BoatClass.class, withSettings().serializable());
        when(race.getBoatClass()).thenReturn(boatClass);
        when(boatClass.typicallyStartsUpwind()).thenReturn(true);
        when(race.getCourse()).thenReturn(course);
        when(race.getCompetitors()).thenReturn(Collections.<Competitor>emptySet());
        trackedRace = new DynamicTrackedRaceImpl(trackedRegatta, race, Collections.<Sideline> emptySet(),
                EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE,
                /* delayToLiveInMillis */10000, /* millisecondsOverWhichToAverageWind */30000, /* millisecondsOverWhichToAverageSpeed */
                7000) {
            private static final long serialVersionUID = 9114777576548711763L;

            @Override
            public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
                try {
                    addCalls.put(zeroBasedIndex, waypointThatGotAdded);
                } catch (NullPointerException npe) {
                    // if the reference to the enclosing object hasn't been de-serialized yet, don't fail
                }
                super.waypointAdded(zeroBasedIndex, waypointThatGotAdded);
            }

            @Override
            public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
                try {
                    removeCalls.put(zeroBasedIndex, waypointThatGotRemoved);
                } catch (NullPointerException npe) {
                    // if the reference to the enclosing object hasn't been de-serialized yet, don't fail
                }
                super.waypointRemoved(zeroBasedIndex, waypointThatGotRemoved);
            }
        };
        trackedRaceWaypoints = new TrackedRaceAsWaypointList(trackedRace);
    }
    
    @Test
    public void simpleStructureConsistencyTest() {
        // don't test one waypoint only because the tracked race will report an empty waypoint list in this case
        // because there is no leg
        for (int numberOfWaypoints = 0; numberOfWaypoints < 5; numberOfWaypoints+=2) {
            setUp(numberOfWaypoints);
            Iterable<Waypoint> courseWaypoints = course.getWaypoints();
            assertEquals(courseWaypoints, trackedRaceWaypoints);
            assertTrue(addCalls.isEmpty());
            assertTrue(removeCalls.isEmpty());
        }
    }

    @Test
    public void testSingleWaypoint() throws PatchFailedException {
        // with one waypoint only the tracked race will also report the single waypoint because the
        // mark passings collection has it in its key set; so there will be no difference between the
        // two lists.
        setUp(1);
        Iterable<Waypoint> courseWaypoints = course.getWaypoints();
        Patch<Waypoint> patch = DiffUtils.diff(trackedRaceWaypoints, courseWaypoints);
        patch.applyToInPlace(trackedRaceWaypoints);
        assertTrue(Util.isEmpty(trackedRace.getTrackedLegs()));
        assertTrue(addCalls.isEmpty());
    }
    
    @Test
    public void testCourseUpdateEndToEnd() throws PatchFailedException {
        setUp(5);
        assertEquals(5, Util.size(course.getWaypoints()));
        course.addWaypoint(2, new WaypointImpl(new MarkImpl("Mark 2.5")));
        assertEquals(6, Util.size(course.getWaypoints()));
        assertEquals(course.getWaypoints(), trackedRaceWaypoints);
        assertEquals(course.getLegs().size(), Util.size(trackedRace.getTrackedLegs()));
    }
    
    private static class ObjectInputStreamWithLocalClassResolving extends ObjectInputStream {
        public ObjectInputStreamWithLocalClassResolving(InputStream is) throws IOException {
            super(is);
        }
        
        @Override
        protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
            String className = classDesc.getName();
            // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6554519
            // If using loadClass(...) on the class loader directly, an exception is thrown:
            // StreamCorruptedException: invalid type code 00
            try {
                return Class.forName(className, /* initialize */ true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException cnfe) {
                return super.resolveClass(classDesc);
            }
        }
    }
    
    @Test
    public void testAddingOneWaypointHalfWayIntoSerialization() throws IOException, ClassNotFoundException {
        setUp(5);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(course); // now the course has been written to the stream and won't be written a second time
        // update the course:
        course.addWaypoint(2, new WaypointImpl(new MarkImpl("Mark 2.5")));
        assertEquals(course.getLegs().size(), Util.size(trackedRace.getTrackedLegs()));
        oos.writeObject(trackedRace);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStreamWithLocalClassResolving(bis);
        Course deserializedCourse = (Course) ois.readObject();
        TrackedRaceImpl deserializedTrackedRace = (TrackedRaceImpl) ois.readObject();
        ois.close();
        assertFalse(course.getWaypoints().equals(deserializedCourse.getWaypoints())); // the deserialized course is expected to have one fewer waypoint
        assertEquals(Util.size(course.getWaypoints())-1, Util.size(deserializedCourse.getWaypoints()));
        assertEquals(Util.size(deserializedTrackedRace.getTrackedLegs())+1, Util.size(deserializedTrackedRace.getRace().getCourse().getWaypoints()));
    }

    @Test
    public void testAddingTwoWaypointsHalfWayIntoSerialization() throws IOException, ClassNotFoundException {
        setUp(5);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(course); // now the course has been written to the stream and won't be written a second time
        // update the course:
        course.addWaypoint(2, new WaypointImpl(new MarkImpl("Mark 2.5")));
        course.addWaypoint(4, new WaypointImpl(new MarkImpl("Mark 4.5")));
        assertEquals(course.getLegs().size(), Util.size(trackedRace.getTrackedLegs()));
        oos.writeObject(trackedRace);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStreamWithLocalClassResolving(bis);
        Course deserializedCourse = (Course) ois.readObject();
        TrackedRaceImpl deserializedTrackedRace = (TrackedRaceImpl) ois.readObject();
        ois.close();
        assertFalse(course.getWaypoints().equals(deserializedCourse.getWaypoints())); // the deserialized course is expected to have one fewer waypoint
        assertEquals(Util.size(course.getWaypoints())-2, Util.size(deserializedCourse.getWaypoints()));
        assertEquals(Util.size(deserializedTrackedRace.getTrackedLegs())+1, Util.size(deserializedTrackedRace.getRace().getCourse().getWaypoints()));
    }

    @Test
    public void testRemovingOneWaypointHalfWayIntoSerialization() throws IOException, ClassNotFoundException {
        setUp(5);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(course); // now the course has been written to the stream and won't be written a second time
        // update the course:
        course.removeWaypoint(2);
        assertEquals(course.getLegs().size(), Util.size(trackedRace.getTrackedLegs()));
        oos.writeObject(trackedRace);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStreamWithLocalClassResolving(bis);
        Course deserializedCourse = (Course) ois.readObject();
        TrackedRaceImpl deserializedTrackedRace = (TrackedRaceImpl) ois.readObject();
        ois.close();
        assertFalse(course.getWaypoints().equals(deserializedCourse.getWaypoints())); // the deserialized course is expected to have one more waypoint
        assertEquals(Util.size(course.getWaypoints())+1, Util.size(deserializedCourse.getWaypoints()));
        assertEquals(Util.size(deserializedTrackedRace.getTrackedLegs())+1, Util.size(deserializedTrackedRace.getRace().getCourse().getWaypoints()));
    }

    @Test
    public void testRemovingTwoWaypointsHalfWayIntoSerialization() throws IOException, ClassNotFoundException {
        setUp(5);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(course); // now the course has been written to the stream and won't be written a second time
        // update the course:
        course.removeWaypoint(2);
        course.removeWaypoint(3);
        assertEquals(course.getLegs().size(), Util.size(trackedRace.getTrackedLegs()));
        oos.writeObject(trackedRace);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStreamWithLocalClassResolving(bis);
        Course deserializedCourse = (Course) ois.readObject();
        TrackedRaceImpl deserializedTrackedRace = (TrackedRaceImpl) ois.readObject();
        ois.close();
        assertFalse(course.getWaypoints().equals(deserializedCourse.getWaypoints())); // the deserialized course is expected to have one fewer waypoint
        assertEquals(Util.size(course.getWaypoints())+2, Util.size(deserializedCourse.getWaypoints()));
        assertEquals(Util.size(deserializedTrackedRace.getTrackedLegs())+1, Util.size(deserializedTrackedRace.getRace().getCourse().getWaypoints()));
    }

    @Test
    public void testAddingTwoAndRemovingOneWaypointHalfWayIntoSerialization() throws IOException, ClassNotFoundException {
        setUp(5);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(course); // now the course has been written to the stream and won't be written a second time
        // update the course:
        course.addWaypoint(2, new WaypointImpl(new MarkImpl("Mark 2.5")));
        course.removeWaypoint(3);
        course.addWaypoint(4, new WaypointImpl(new MarkImpl("Mark 4.5")));
        assertEquals(course.getLegs().size(), Util.size(trackedRace.getTrackedLegs()));
        oos.writeObject(trackedRace);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStreamWithLocalClassResolving(bis);
        Course deserializedCourse = (Course) ois.readObject();
        TrackedRaceImpl deserializedTrackedRace = (TrackedRaceImpl) ois.readObject();
        ois.close();
        assertFalse(course.getWaypoints().equals(deserializedCourse.getWaypoints())); // the deserialized course is expected to have one fewer waypoint
        assertEquals(Util.size(course.getWaypoints())-1, Util.size(deserializedCourse.getWaypoints()));
        assertEquals(Util.size(deserializedTrackedRace.getTrackedLegs())+1, Util.size(deserializedTrackedRace.getRace().getCourse().getWaypoints()));
    }

}
