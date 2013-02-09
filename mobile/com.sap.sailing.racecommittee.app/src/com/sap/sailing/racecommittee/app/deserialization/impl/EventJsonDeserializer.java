package com.sap.sailing.racecommittee.app.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;

public class EventJsonDeserializer implements JsonDeserializer<Event> {
	private JsonDeserializer<Venue> venueDeserializer;
	
	public EventJsonDeserializer(JsonDeserializer<Venue> venueDeserializer) {
		this.venueDeserializer = venueDeserializer;
	}
	
	public Event deserialize(JSONObject object) throws JsonDeserializationException {
		String id = object.get(EventJsonSerializer.FIELD_ID).toString();
		String name = object.get(EventJsonSerializer.FIELD_NAME).toString();
		String publicationUrl = object.get(EventJsonSerializer.FIELD_PUBLICATION_URL).toString();
		
		JSONObject venueObject = Helpers.getNestedObjectSafe(
				object, 
				EventJsonSerializer.FIELD_VENUE);
		Venue venue = venueDeserializer.deserialize(venueObject);
		
		return new EventImpl(
				name, 
				venue, 
				publicationUrl, 
				true, 
				Helpers.tryUuidConversion(id));
	}

}
