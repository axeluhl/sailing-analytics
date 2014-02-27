package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;

public class EventDataJsonSerializerTest {

    protected final UUID expectedId = UUID.randomUUID();
    protected final String expectedName = "ab";
    protected final TimePoint expectedStartDate = new MillisecondsTimePoint(new Date());
    protected final TimePoint expectedEndDate = new MillisecondsTimePoint(new Date());

    protected JsonSerializer<Venue> venueSerializer;
    protected EventJsonSerializer serializer;
    protected EventBase event;

    // see https://groups.google.com/forum/?fromgroups=#!topic/mockito/iMumB0_bpdo
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        // Event and its basic attributes ...
        event = mock(EventBase.class);
        when(event.getId()).thenReturn(expectedId);
        when(event.getName()).thenReturn(expectedName);
        when(event.getStartDate()).thenReturn(expectedStartDate);
        when(event.getEndDate()).thenReturn(expectedEndDate);

        // ... and the serializer itself.		
        venueSerializer = mock(JsonSerializer.class);
        serializer = new EventJsonSerializer(venueSerializer);
    }

    @Test
    public void testBasicAttributes() {
        JSONObject result = serializer.serialize(event);

        assertEquals(
                expectedId,
                UUID.fromString(result.get(EventJsonSerializer.FIELD_ID).toString()));
        assertEquals(
                expectedName,
                result.get(EventJsonSerializer.FIELD_NAME));
        assertEquals(
                expectedStartDate,
                new MillisecondsTimePoint(((Number) result.get(EventJsonSerializer.FIELD_START_DATE)).longValue()));
        assertEquals(
                expectedEndDate,
                new MillisecondsTimePoint(((Number) result.get(EventJsonSerializer.FIELD_END_DATE)).longValue()));
    }

    @Test
    public void testSerializesVenue() {
        // ... venue and its mocked serializer ...
        Venue venue = mock(Venue.class);
        when(event.getVenue()).thenReturn(venue);
        JSONObject expectedVenue = new JSONObject();

        when(venueSerializer.serialize(eq(venue))).thenReturn(expectedVenue);

        JSONObject result = serializer.serialize(event);

        assertEquals(
                expectedVenue,
                result.get(EventJsonSerializer.FIELD_VENUE));
    }

}
