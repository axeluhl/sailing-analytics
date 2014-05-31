package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
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
        final Venue venue;
        if (object.get(EventJsonSerializer.FIELD_VENUE) != null) {
            JSONObject venueObject = Helpers.getNestedObjectSafe(object, EventJsonSerializer.FIELD_VENUE);
            venue = venueDeserializer.deserialize(venueObject);
        } else {
            venue = null;
        }
        EventBaseImpl result = new EventBaseImpl(name, startDate == null ? null : new MillisecondsTimePoint(startDate.longValue()),
                endDate == null ? null : new MillisecondsTimePoint(endDate.longValue()), venue, true, id);
        if (object.get(EventJsonSerializer.FIELD_IMAGE_URLS) != null) {
            try {
                result.setImageURLs(getURLsFromStrings(Helpers.getNestedArraySafe(object, EventJsonSerializer.FIELD_IMAGE_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing image URLs for event "+name, e);
            }
        }
        if (object.get(EventJsonSerializer.FIELD_VIDEO_URLS) != null) {
            try {
                result.setVideoURLs(getURLsFromStrings(Helpers.getNestedArraySafe(object, EventJsonSerializer.FIELD_VIDEO_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing video URLs for event "+name, e);
            }
        }
        return result;
    }
    
    private Iterable<URL> getURLsFromStrings(JSONArray strings) throws MalformedURLException {
        List<URL> result = new ArrayList<URL>();
        if (strings != null) {
            for (Object string : strings) {
                result.add(new URL(string.toString()));
            }
        }
        return result;
    }

}
