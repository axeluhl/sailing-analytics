package com.sap.sailing.domain.racelog.test;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.tracking.NotRevokableException;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;

public class RaceLogRevocationTest {
    private RaceLog serverRaceLog;
    private RaceLogEventAuthor serverAuthor = new RaceLogEventAuthorImpl("server", 0);
    private RaceLog clientARaceLog;
    private UUID clientAUUID = UUID.randomUUID();
    private RaceLogEventAuthor clientAAuthor = new RaceLogEventAuthorImpl("clientA", 1);
    private RaceLog clientBRaceLog;
    private UUID clientBUUID = UUID.randomUUID();
    private RaceLogEventAuthor clientBAuthor = new RaceLogEventAuthorImpl("clientB", 2);
    
    @Before
    public void setup() {
        serverRaceLog = new RaceLogImpl("server");
        clientARaceLog = new RaceLogImpl("clientA");
        clientBRaceLog = new RaceLogImpl("clientB");
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
    
    @Test
    public void sameEventRevokedByTwoClientsBeforeSync() throws NotRevokableException {
        DenoteForTrackingEvent event = RaceLogEventFactory.INSTANCE.createDenoteForTrackingEvent(
                now(), serverAuthor, 0, "race", null, null);
        serverRaceLog.add(event);
        
        //sync events to clients
        sendClientAEvent(null);
        sendClientBEvent(null);
        
        //they revoke the event independently
        clientARaceLog.revokeEvent(clientAAuthor, event);
        clientBRaceLog.revokeEvent(clientBAuthor, event);
    }
}
