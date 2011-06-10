package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class CourseUpdateTest extends AbstractTracTracLiveTest {
    private Course course;
    private Event domainEvent;
    private DynamicTrackedEvent trackedEvent;
    
    public CourseUpdateTest() throws URISyntaxException, MalformedURLException {
        super();
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException {
        super.setUp();
        domainEvent = DomainFactory.INSTANCE.createEvent(getEvent());
        trackedEvent = DomainFactory.INSTANCE.trackEvent(domainEvent);
        Iterable<Receiver> myReceivers = DomainFactory.INSTANCE.getUpdateReceivers(trackedEvent, getEvent(),
                EmptyWindStore.INSTANCE, ReceiverType.RACECOURSE);
        addListenersForStoredDataAndStartController(myReceivers);
        RaceDefinition race = DomainFactory.INSTANCE.getRaceDefinition(getEvent().getRaceList().iterator().next());
        course = race.getCourse();
        assertNotNull(course);
        assertEquals(3, Util.size(course.getWaypoints()));
    }
    
    @Test
    public void testWaypointListDiff() {
        Waypoint wp1 = new WaypointImpl(new BuoyImpl("b1"));
        Waypoint wp2 = new WaypointImpl(new BuoyImpl("b2"));
        Waypoint wp3 = new WaypointImpl(new BuoyImpl("b3"));
        Waypoint wp4 = new WaypointImpl(new BuoyImpl("b4"));
        List<Waypoint> waypoints = new ArrayList<Waypoint>(4);
        waypoints.add(wp1);
        waypoints.add(wp2);
        waypoints.add(wp3);
        waypoints.add(wp4);
        List<Waypoint> changedWaypoints = new ArrayList<Waypoint>(3);
        changedWaypoints.add(wp1);
        changedWaypoints.add(wp3);
        changedWaypoints.add(wp4);
        
        Patch<Waypoint> patch = DiffUtils.diff(waypoints, changedWaypoints);
        assertEquals(1, patch.getDeltas().size());
        Delta<Waypoint> firstDelta = patch.getDeltas().iterator().next();
        assertEquals(Delta.TYPE.DELETE, firstDelta.getType());
        Chunk<Waypoint> original = firstDelta.getOriginal();
        assertEquals(1, original.getPosition());
        List<Waypoint> deletedWaypoints = original.getLines();
        assertEquals(1, deletedWaypoints.size());
        assertEquals(wp2, deletedWaypoints.iterator().next());
    }
    
    @Test
    public void testLastWaypointRemoved() {
        
    }
}
