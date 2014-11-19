package com.sap.sailing.domain.racelog.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.racelog.tracking.NotRevokableException;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RevokeEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceLogRevocationTest {
    private RaceLog serverRaceLog;
    private RaceLogEventAuthor serverAuthor = new RaceLogEventAuthorImpl("server", 2);
    private RaceLog clientARaceLog;
    private UUID clientAUUID = UUID.randomUUID();
    private RaceLogEventAuthor clientAAuthor = new RaceLogEventAuthorImpl("clientA", 3);
    private RaceLog clientBRaceLog;
    private UUID clientBUUID = UUID.randomUUID();
    private RaceLogEventAuthor clientBAuthor = new RaceLogEventAuthorImpl("clientB", 1);
    private RaceLog clientCRaceLog;
    private UUID clientCUUID = UUID.randomUUID();
    private RaceLogEventAuthor clientCAuthor = new RaceLogEventAuthorImpl("clientC", 0);
    
    @Before
    public void setup() {
        serverRaceLog = new RaceLogImpl("server");
        clientARaceLog = new RaceLogImpl("clientA");
        clientBRaceLog = new RaceLogImpl("clientB");
        clientCRaceLog = new RaceLogImpl("clientC");
    }
    
    private static TimePoint now() {
        return MillisecondsTimePoint.now();
    }
    
    private void sendClientEvent(RaceLog clientRaceLog, RaceLogEvent clientEvent,
            UUID clientUUID) {
        Iterable<RaceLogEvent> eventsToAdd = null;
        if (clientEvent == null) {
            eventsToAdd = serverRaceLog.getEventsToDeliver(clientUUID);
        } else {
            eventsToAdd = serverRaceLog.add(clientEvent, clientUUID);
        }
        for (RaceLogEvent eventToAdd : eventsToAdd) {
            clientRaceLog.add(eventToAdd);
        }
    }
    
    private void sendClientAEvent(RaceLogEvent event) {
        sendClientEvent(clientARaceLog, event, clientAUUID);
    }
    
    private void sendClientBEvent(RaceLogEvent event) {
        sendClientEvent(clientBRaceLog, event, clientBUUID);
    }
    
    private void sendClientCEvent(RaceLogEvent event) {
        sendClientEvent(clientCRaceLog, event, clientCUUID);
    }
    
    private static void assertNumUnrevoked(RaceLog raceLog, int expectedNum) {
        raceLog.lockForRead();
        try {
            assertEquals(expectedNum, Util.size(raceLog.getUnrevokedEvents()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }
    
    private static void assertNumAll(RaceLog raceLog, int expectedNum) {
        raceLog.lockForRead();
        try {
            assertEquals(expectedNum, Util.size(raceLog.getRawFixes()));
        } finally {
            raceLog.unlockAfterRead();
        }
    }
    
    @Test
    public void sameEventRevokedByTwoClientsBeforeSync() throws NotRevokableException {
        DenoteForTrackingEvent event = RaceLogEventFactory.INSTANCE.createDenoteForTrackingEvent(
                now(), serverAuthor, 0, "race", null, null);
        serverRaceLog.add(event);
        
        //sync events to clients
        sendClientAEvent(null);
        sendClientBEvent(null);
        sendClientCEvent(null);
        
        //they revoke the event independently
        try {
            clientARaceLog.revokeEvent(clientAAuthor, event);
        } catch (NotRevokableException e) {
            //expected, because A does not have sufficient priority
        }
        RevokeEvent revokeEventB = clientBRaceLog.revokeEvent(clientBAuthor, event);
        RevokeEvent revokeEventC = clientCRaceLog.revokeEvent(clientCAuthor, event);
        
        sendClientBEvent(revokeEventB);
        sendClientCEvent(revokeEventC);
        
        //update clients
        sendClientAEvent(null);
        sendClientBEvent(null);
        sendClientCEvent(null);
        
        //check if the event has been revoked on the server and clients
        assertNumUnrevoked(clientARaceLog, 0);
        assertNumAll(clientARaceLog, 3);
        assertNumUnrevoked(clientBRaceLog, 0);
        assertNumAll(clientBRaceLog, 3);
        assertNumUnrevoked(clientCRaceLog, 0);
        assertNumAll(clientCRaceLog, 3);
        assertNumUnrevoked(serverRaceLog, 0);
        assertNumAll(serverRaceLog, 3);
    }
}
