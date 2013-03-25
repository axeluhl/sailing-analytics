package com.sap.sailing.racecommittee.app.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.EventBaseImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.EventDataJsonSerializer;

public class EventDataJsonDeserializer implements JsonDeserializer<EventBase> {
	private JsonDeserializer<Venue> venueDeserializer;
	
	public EventDataJsonDeserializer(JsonDeserializer<Venue> venueDeserializer) {
		this.venueDeserializer = venueDeserializer;
	}
	
	public EventBase deserialize(JSONObject object) throws JsonDeserializationException {
		String id = object.get(EventDataJsonSerializer.FIELD_ID).toString();
		String name = object.get(EventDataJsonSerializer.FIELD_NAME).toString();
		String publicationUrl = object.get(EventDataJsonSerializer.FIELD_PUBLICATION_URL).toString();
		
		JSONObject venueObject = Helpers.getNestedObjectSafe(
				object, 
				EventDataJsonSerializer.FIELD_VENUE);
		Venue venue = venueDeserializer.deserialize(venueObject);
		
		return new EventBaseImpl(
				name, 
				venue, 
				publicationUrl, 
				true, 
				Helpers.tryUuidConversion(id));
	}

}
