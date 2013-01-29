package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class EventJsonSerializer implements JsonSerializer<Event> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_PUBLICATION_URL = "publicationUrl";
	public static final String FIELD_VENUE = "venue";
	
	private JsonSerializer<Venue> venueSerializer;
	
	public EventJsonSerializer(JsonSerializer<Venue> venueSerializer)
	{
		this.venueSerializer = venueSerializer;
	}

	public JSONObject serialize(Event object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_PUBLICATION_URL, object.getPublicationUrl());
		result.put(FIELD_VENUE, venueSerializer.serialize(object.getVenue()));
		
		return result;
	}
}
