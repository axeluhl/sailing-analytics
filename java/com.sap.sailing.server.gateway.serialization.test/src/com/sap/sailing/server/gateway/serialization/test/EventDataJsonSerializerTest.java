package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

public class EventDataJsonSerializerTest {
    protected final UUID expectedId = UUID.randomUUID();
    protected final String expectedName = "ab";
    protected final TimePoint expectedStartDate = new MillisecondsTimePoint(new Date());
    protected final TimePoint expectedEndDate = new MillisecondsTimePoint(new Date());
    protected final Venue expectedVenue = new VenueImpl("Expected Venue");

    protected JsonSerializer<Venue> venueSerializer;
    protected EventJsonSerializer serializer;
    protected EventBaseJsonDeserializer deserializer;
    protected EventBase event;

    // see https://groups.google.com/forum/?fromgroups=#!topic/mockito/iMumB0_bpdo
    @Before
    public void setUp() {
        // Event and its basic attributes ...
        event = mock(EventBase.class);
        when(event.getId()).thenReturn(expectedId);
        when(event.getName()).thenReturn(expectedName);
        when(event.getStartDate()).thenReturn(expectedStartDate);
        when(event.getEndDate()).thenReturn(expectedEndDate);
        when(event.getVenue()).thenReturn(expectedVenue);
        // ... and the serializer itself.		
        serializer = new EventJsonSerializer(new VenueJsonSerializer(new CourseAreaJsonSerializer()));
        deserializer = new EventBaseJsonDeserializer(new VenueJsonDeserializer(new CourseAreaJsonDeserializer(DomainFactory.INSTANCE)));
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
    public void testBasicAttributesAfterDeserialization() throws JsonDeserializationException {
        final JSONObject result = serializer.serialize(event);
        final EventBase deserializedEvent = deserializer.deserialize(result);
        assertEquals(expectedId, deserializedEvent.getId());
        assertEquals(expectedName, deserializedEvent.getName());
        assertEquals(expectedStartDate, deserializedEvent.getStartDate());
        assertEquals(expectedEndDate, deserializedEvent.getEndDate());
    }

    @Test
    public void testSerializesVenue() throws JsonDeserializationException {
        JSONObject result = serializer.serialize(event);
        EventBase event = deserializer.deserialize(result);
        assertEquals(expectedVenue.getName(), event.getVenue().getName());
    }

}
