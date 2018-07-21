package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.mockito.Mockito.mock;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogPassChangeEventSerializer;

public class RaceLogPassChangeEventSerializerTest extends AbstractEventSerializerTest<RaceLogPassChangeEvent> {

    @Override
    protected RaceLogPassChangeEvent createMockEvent() {
        RaceLogPassChangeEvent event = mock(RaceLogPassChangeEvent.class);
        return event;
    }

    @Override
    protected JsonSerializer<RaceLogEvent> createSerializer(JsonSerializer<Competitor> competitorSerializer) {
        return new RaceLogPassChangeEventSerializer(competitorSerializer);
    }

}
