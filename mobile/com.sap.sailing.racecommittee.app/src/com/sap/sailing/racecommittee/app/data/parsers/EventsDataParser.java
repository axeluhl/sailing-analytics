package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class EventsDataParser implements DataParser<Collection<EventData>> {

	private JsonDeserializer<EventData> deserializer;
	
	public EventsDataParser(JsonDeserializer<EventData> deserializer) {
		this.deserializer = deserializer;
	}

	public Collection<EventData> parse(Reader reader) throws Exception {
		Object parsedResult = JSONValue.parse(reader);
		JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);
		Collection<EventData> events = new ArrayList<EventData>();
		
		for (Object element : jsonArray) {
			JSONObject json = Helpers.toJSONObjectSafe(element);
			events.add(deserializer.deserialize(json));
		}
		
		return events;
	}

}
