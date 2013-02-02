package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.impl.Util;

public class EventReplicationTest extends AbstractServerReplicationTest {

	@Test
    public void testEventReplication() throws InterruptedException {
        final String eventName = "ESS Masquat";
        final String venueName = "Masquat, Oman";
        final String publicationUrl = "http://ess40.sapsailing.com";
        final boolean isPublic = false;
        List<String> regattas = new ArrayList<String>();
        regattas.add("Day1");
        regattas.add("Day2");
        Event masterEvent = master.addEvent(eventName, venueName, publicationUrl, isPublic, UUID.randomUUID(), regattas);

        Thread.sleep(1000);
        Event replicatedEvent = replica.getEvent(masterEvent.getId());
        assertNotNull(replicatedEvent);
        assertEquals(replicatedEvent.getName(), eventName);
        assertEquals(replicatedEvent.getPublicationUrl(), publicationUrl);
        assertEquals(replicatedEvent.getVenue().getName(), venueName);
    }
	
	@Test
	public void testCourseAreaReplication() throws InterruptedException {
		final String eventName = "ESS Singapur";
        final String venueName = "Singapur, Singapur";
        final String publicationUrl = "http://ess40.sapsailing.com";
        final boolean isPublic = false;
        List<String> regattas = new ArrayList<String>();
        
        final String courseArea = "Alpha";
        
        Event masterEvent = master.addEvent(eventName, venueName, publicationUrl, isPublic, UUID.randomUUID(), regattas);
        CourseArea masterCourseArea = master.addCourseArea(masterEvent.getId(), courseArea, UUID.randomUUID());
        
        Thread.sleep(1000);
        Event replicatedEvent = replica.getEvent(masterEvent.getId());
        assertNotNull(replicatedEvent);
        assertEquals(replicatedEvent.getName(), eventName);
        assertEquals(Util.size(replicatedEvent.getVenue().getCourseAreas()), 1);
        
        CourseArea replicatedCourseArea = Util.get(replicatedEvent.getVenue().getCourseAreas(), 0);
        assertEquals(replicatedCourseArea.getId(), masterCourseArea.getId());
        assertEquals(replicatedCourseArea.getName(), courseArea);
	}
}
