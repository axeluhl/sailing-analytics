package com.sap.sailing.server.replication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.impl.Util;

public class ReplicationStopTest extends AbstractServerReplicationTest {
    @Test
    public void testReplicaStop() throws Exception {
        assertNotSame(master, replica);
        assertEquals(Util.size(master.getAllRegattas()), Util.size(replica.getAllRegattas()));
        Event event = addEventOnMaster("Test 1 Event");
        Thread.sleep(1000);
        assertNotNull(replica.getEvent(event.getId()));
        stopReplicatingToMaster();
        Event event2 = addEventOnMaster("Test 2 Event");
        assertNull(replica.getEvent(event2.getId()));
    }
    
    private Event addEventOnMaster(String eventName) {
        final String venueName = "Masquat, Oman";
        final String publicationUrl = "http://ess40.sapsailing.com";
        final boolean isPublic = false;
        List<String> regattas = new ArrayList<String>();
        regattas.add("Day1");
        regattas.add("Day2");
        return master.addEvent(eventName, venueName, publicationUrl, isPublic, UUID.randomUUID(), regattas);
    }
    
    
}