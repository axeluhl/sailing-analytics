package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class EventDataJsonSerializer implements JsonSerializer<EventData> {
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_PUBLICATION_URL = "publicationUrl";
	public static final String FIELD_VENUE = "venue";
	
	private final JsonSerializer<Venue> venueSerializer;
	
	public EventDataJsonSerializer(JsonSerializer<Venue> venueSerializer)
	{
		this.venueSerializer = venueSerializer;
	}

	public JSONObject serialize(EventData object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_ID, object.getId().toString());
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_PUBLICATION_URL, object.getPublicationUrl());
		result.put(FIELD_VENUE, venueSerializer.serialize(object.getVenue()));
		
		return result;
	}
}
