package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.BaseRaceLogEventSerializer;

public abstract class BaseRaceLogEventTest<FlagType extends RaceLogEvent> {

    protected final UUID expectedId = UUID.randomUUID();
    protected final long expectedTimestamp = 1337l;
    protected final int expectedPassId = 42;

    protected JsonSerializer<RaceLogEvent> serializer;
    protected FlagType event;

    protected abstract FlagType createMockEvent();

    protected abstract JsonSerializer<RaceLogEvent> createSerializer();

    @Before
    public void setUp() {
        serializer = createSerializer();
        event = createMockEvent();

        TimePoint timePoint = mock(TimePoint.class);
        when(timePoint.asMillis()).thenReturn(expectedTimestamp);

        when(event.getId()).thenReturn(expectedId);
        when(event.getTimePoint()).thenReturn(timePoint);
        when(event.getPassId()).thenReturn(expectedPassId);
        when(event.getInvolvedBoats()).thenReturn(
                Collections.<Competitor> emptyList());
    }

    @Test
    public void testSerializeBaseAttributes() {
        JSONObject json = serializer.serialize(event);

        assertEquals(
                expectedId,
                UUID.fromString(json.get(
                        BaseRaceLogEventSerializer.FIELD_ID).toString()));
        assertEquals(expectedTimestamp,
                json.get(BaseRaceLogEventSerializer.FIELD_TIMESTAMP));
        assertEquals(expectedPassId,
                json.get(BaseRaceLogEventSerializer.FIELD_PASS_ID));
    }

}
