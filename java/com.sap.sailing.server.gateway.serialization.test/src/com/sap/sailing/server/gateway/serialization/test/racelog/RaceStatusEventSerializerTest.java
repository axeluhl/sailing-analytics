package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogRaceStatusEventSerializer;

public class RaceStatusEventSerializerTest extends AbstractEventSerializerTest<RaceLogRaceStatusEvent> {

    private RaceLogRaceStatus expectedStatus = RaceLogRaceStatus.STARTPHASE;
    
    @Override
    protected RaceLogRaceStatusEvent createMockEvent() {
        RaceLogRaceStatusEvent event = mock(RaceLogRaceStatusEvent.class);
        setupMockEvent(event);
        return event;
    }

    protected void setupMockEvent(RaceLogRaceStatusEvent event) {
        when(event.getNextStatus()).thenReturn(expectedStatus);
    }

    @Override
    protected JsonSerializer<RaceLogEvent> createSerializer(JsonSerializer<Competitor> competitorSerializer) {
        return new RaceLogRaceStatusEventSerializer(competitorSerializer);
    }

}
