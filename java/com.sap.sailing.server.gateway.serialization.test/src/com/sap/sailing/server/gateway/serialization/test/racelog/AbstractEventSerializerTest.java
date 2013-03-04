package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.BaseRaceLogEventSerializer;

public abstract class AbstractEventSerializerTest<EventType extends RaceLogEvent> {

    protected final UUID expectedId = UUID.randomUUID();
    protected final long expectedTimestamp = 1337l;
    protected final int expectedPassId = 42;

    private JsonSerializer<Competitor> competitorSerializer;
    protected JsonSerializer<RaceLogEvent> serializer;
    protected EventType event;

    protected abstract EventType createMockEvent();

    protected abstract JsonSerializer<RaceLogEvent> createSerializer(JsonSerializer<Competitor> competitorSerializer);

    /**
     * Be sure to call this from subclasses as last statement!
     */
    @Before
    public void setUp() {
        serializer = createSerializer(competitorSerializer);
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
    
    @Test
    public void testIsParseable() throws ParseException {
        JSONObject json = serializer.serialize(event);
        String value = json.toJSONString();
        JSONValue.parseWithException(value);
    }

}
