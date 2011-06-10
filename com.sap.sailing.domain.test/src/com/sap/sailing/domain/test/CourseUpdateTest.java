package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.Receiver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.util.Util;

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
    public void testLastWaypointRemoved() {
        
    }
}
