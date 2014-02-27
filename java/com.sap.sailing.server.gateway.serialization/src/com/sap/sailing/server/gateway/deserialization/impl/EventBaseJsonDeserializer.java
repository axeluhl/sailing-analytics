package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.EventBaseImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.EventJsonSerializer;

public class EventBaseJsonDeserializer implements JsonDeserializer<EventBase> {
    private JsonDeserializer<Venue> venueDeserializer;

    public EventBaseJsonDeserializer(JsonDeserializer<Venue> venueDeserializer) {
        this.venueDeserializer = venueDeserializer;
    }

    public EventBase deserialize(JSONObject object) throws JsonDeserializationException {
        UUID id = UUID.fromString((String) object.get(EventJsonSerializer.FIELD_ID));
        String name = object.get(EventJsonSerializer.FIELD_NAME).toString();
        Number startDate = (Number) object.get(EventJsonSerializer.FIELD_START_DATE);
        Number endDate = (Number) object.get(EventJsonSerializer.FIELD_END_DATE);
        JSONObject venueObject = Helpers.getNestedObjectSafe(object, EventJsonSerializer.FIELD_VENUE);
        Venue venue = venueDeserializer.deserialize(venueObject);
        return new EventBaseImpl(name, new MillisecondsTimePoint(startDate.longValue()), new MillisecondsTimePoint(endDate.longValue()), 
                venue, true, id);
    }

}
