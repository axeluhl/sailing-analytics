package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class EventJsonSerializer implements JsonSerializer<EventBase> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_VENUE = "venue";

    private final JsonSerializer<Venue> venueSerializer;

    public EventJsonSerializer(JsonSerializer<Venue> venueSerializer)
    {
        this.venueSerializer = venueSerializer;
    }

    public JSONObject serialize(EventBase event) {
        JSONObject result = new JSONObject();

        result.put(FIELD_ID, event.getId().toString());
        result.put(FIELD_NAME, event.getName());
        result.put(FIELD_START_DATE, event.getStartDate().asMillis());
        result.put(FIELD_END_DATE, event.getEndDate().asMillis());
        result.put(FIELD_VENUE, venueSerializer.serialize(event.getVenue()));

        return result;
    }
}
