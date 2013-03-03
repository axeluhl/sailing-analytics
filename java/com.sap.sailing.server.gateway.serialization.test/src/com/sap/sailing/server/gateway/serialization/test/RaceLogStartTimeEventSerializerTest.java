package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogStartTimeEventSerializer;

public class RaceLogStartTimeEventSerializerTest extends BaseRaceLogEventTest<RaceLogStartTimeEvent> {

    private final long expectedStartTimeTimestamp = 2013;
    private final RaceLogRaceStatus expectedNextStatus = RaceLogRaceStatus.SCHEDULED;

    @Override
    protected RaceLogStartTimeEvent createMockEvent() {

        TimePoint startTime = mock(TimePoint.class);
        when(startTime.asMillis()).thenReturn(expectedStartTimeTimestamp);

        RaceLogStartTimeEvent event = mock(RaceLogStartTimeEvent.class);
        when(event.getNextStatus()).thenReturn(expectedNextStatus);
        when(event.getStartTime()).thenReturn(startTime);

        return event;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected JsonSerializer<RaceLogEvent> createSerializer() {
        return new RaceLogStartTimeEventSerializer(mock(JsonSerializer.class));
    }

    @Test
    public void testStartTimeAttribute() {
        JSONObject json = serializer.serialize(event);

        assertEquals(
                expectedStartTimeTimestamp,
                json.get(RaceLogStartTimeEventSerializer.FIELD_START_TIME));
    }
    
    @Test
    public void testNextStatusAttribute() {
        JSONObject json = serializer.serialize(event);

        assertEquals(
                expectedNextStatus,
                RaceLogRaceStatus.valueOf(json.get(RaceLogStartTimeEventSerializer.FIELD_NEXT_STATUS).toString()));
    }

    @Test
    public void testClassAttribute() {
        JSONObject json = serializer.serialize(event);

        assertEquals(
                RaceLogStartTimeEventSerializer.VALUE_CLASS,
                json.get(RaceLogStartTimeEventSerializer.FIELD_CLASS));
    }

}
