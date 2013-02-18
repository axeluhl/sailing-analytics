package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.EventDataJsonSerializer;

public class EventJsonSerializerTest {
	
	protected final UUID expectedId = UUID.randomUUID();
	protected final String expectedName = "ab";
	protected final String expectedPublicationUrl = "cd";
	
	protected JsonSerializer<Venue> venueSerializer;
	protected EventDataJsonSerializer serializer;
	protected EventData event;
	
	// see https://groups.google.com/forum/?fromgroups=#!topic/mockito/iMumB0_bpdo
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		// Event and its basic attributes ...
		event = mock(EventData.class);
		when(event.getId()).thenReturn(expectedId);
		when(event.getName()).thenReturn(expectedName);
		when(event.getPublicationUrl()).thenReturn(expectedPublicationUrl);
		
		// ... and the serializer itself.		
		venueSerializer = mock(JsonSerializer.class);
		serializer = new EventDataJsonSerializer(venueSerializer);
	}
	
	@Test
	public void testBasicAttributes() {
		JSONObject result = serializer.serialize(event);
		
		assertEquals(
				expectedId,
				UUID.fromString(result.get(EventDataJsonSerializer.FIELD_ID).toString()));
		assertEquals(
				expectedName,
				result.get(EventDataJsonSerializer.FIELD_NAME));
		assertEquals(
				expectedPublicationUrl,
				result.get(EventDataJsonSerializer.FIELD_PUBLICATION_URL));
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
				result.get(EventDataJsonSerializer.FIELD_VENUE));
	}

}
