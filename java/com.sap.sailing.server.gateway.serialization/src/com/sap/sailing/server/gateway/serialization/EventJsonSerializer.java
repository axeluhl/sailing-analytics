package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;

public class EventJsonSerializer implements JsonSerializer<Event>
{
	private JsonSerializer<Venue> venueSerializer;
	
	public EventJsonSerializer(JsonSerializer<Venue> venueSerializer)
	{
		this.venueSerializer = venueSerializer;
	}

	public JSONObject serialize(Event object) {
		JSONObject result = new JSONObject();
		
		result.put("id", object.getId().toString());
		result.put("name", object.getName());
		result.put("publicationUrl", object.getPublicationUrl());
		result.put("venue", venueSerializer.serialize(object.getVenue()));
		
		return result;
	}
}
