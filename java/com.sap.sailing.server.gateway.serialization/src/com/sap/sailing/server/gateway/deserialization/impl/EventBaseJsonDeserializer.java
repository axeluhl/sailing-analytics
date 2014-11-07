package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.StrippedEventImpl;
import com.sap.sailing.domain.common.impl.ImageSizeImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class EventBaseJsonDeserializer implements JsonDeserializer<EventBase> {
    private final JsonDeserializer<Venue> venueDeserializer;
    private final JsonDeserializer<LeaderboardGroupBase> leaderboardGroupDeserializer;

    public EventBaseJsonDeserializer(JsonDeserializer<Venue> venueDeserializer, JsonDeserializer<LeaderboardGroupBase> leaderboardGroupDeserializer) {
        this.venueDeserializer = venueDeserializer;
        this.leaderboardGroupDeserializer = leaderboardGroupDeserializer;
    }

    public EventBase deserialize(JSONObject object) throws JsonDeserializationException {
        UUID id = UUID.fromString((String) object.get(EventBaseJsonSerializer.FIELD_ID));
        String name = (String) object.get(EventBaseJsonSerializer.FIELD_NAME);
        String description = (String) object.get(EventBaseJsonSerializer.FIELD_DESCRIPTION);
        String officialWebsiteURLAsString = (String) object.get(EventBaseJsonSerializer.FIELD_OFFICIAL_WEBSITE_URL);
        String logoImageURLAsString = (String) object.get(EventBaseJsonSerializer.FIELD_LOGO_IMAGE_URL);
        Number startDate = (Number) object.get(EventBaseJsonSerializer.FIELD_START_DATE);
        Number endDate = (Number) object.get(EventBaseJsonSerializer.FIELD_END_DATE);
        final Venue venue;
        if (object.get(EventBaseJsonSerializer.FIELD_VENUE) != null) {
            JSONObject venueObject = Helpers.getNestedObjectSafe(object, EventBaseJsonSerializer.FIELD_VENUE);
            venue = venueDeserializer.deserialize(venueObject);
        } else {
            venue = null;
        }
        JSONArray leaderboardGroupsJson = (JSONArray) object.get(EventBaseJsonSerializer.FIELDS_LEADERBOARD_GROUPS);
        List<LeaderboardGroupBase> leaderboardGroups = new ArrayList<LeaderboardGroupBase>();
        if (leaderboardGroupsJson != null) {
            for (Object lgJson : leaderboardGroupsJson) {
                leaderboardGroups.add(leaderboardGroupDeserializer.deserialize((JSONObject) lgJson));
            }
        }
        StrippedEventImpl result = new StrippedEventImpl(name, startDate == null ? null : new MillisecondsTimePoint(startDate.longValue()),
                endDate == null ? null : new MillisecondsTimePoint(endDate.longValue()), venue, /* is public */ true, id, leaderboardGroups);
        result.setDescription(description);
        if (officialWebsiteURLAsString != null) {
            try {
                result.setOfficialWebsiteURL(new URL(officialWebsiteURLAsString));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing official website URL for event "+name, e);
            }
        }
        if (logoImageURLAsString != null) {
            try {
                result.setLogoImageURL(new URL(logoImageURLAsString));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing logo image URL for event "+name, e);
            }
        }
        if (object.get(EventBaseJsonSerializer.FIELD_IMAGE_URLS) != null) {
            try {
                result.setImageURLs(getURLsFromStrings(Helpers.getNestedArraySafe(object, EventBaseJsonSerializer.FIELD_IMAGE_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing image URLs for event "+name, e);
            }
        }
        if (object.get(EventBaseJsonSerializer.FIELD_VIDEO_URLS) != null) {
            try {
                result.setVideoURLs(getURLsFromStrings(Helpers.getNestedArraySafe(object, EventBaseJsonSerializer.FIELD_VIDEO_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing video URLs for event "+name, e);
            }
        }
        if (object.get(EventBaseJsonSerializer.FIELD_SPONSOR_IMAGE_URLS) != null) {
            try {
                result.setSponsorImageURLs(getURLsFromStrings(Helpers.getNestedArraySafe(object, EventBaseJsonSerializer.FIELD_SPONSOR_IMAGE_URLS)));
            } catch (MalformedURLException e) {
                throw new JsonDeserializationException("Error deserializing sponsor image URLs for event "+name, e);
            }
        }
        JSONArray imageSizes = (JSONArray) object.get(EventBaseJsonSerializer.FIELD_IMAGE_SIZES);
        if (imageSizes != null) {
            for (Object imageURLAndSizeObject : imageSizes) {
                JSONObject imageURLAndSizeJson = (JSONObject) imageURLAndSizeObject;
                try {
                    result.setImageSize(
                            new URL((String) imageURLAndSizeJson.get(EventBaseJsonSerializer.FIELD_IMAGE_URL)),
                            new ImageSizeImpl(
                                    ((Number) imageURLAndSizeJson.get(EventBaseJsonSerializer.FIELD_IMAGE_WIDTH)).intValue(),
                                    ((Number) imageURLAndSizeJson.get(EventBaseJsonSerializer.FIELD_IMAGE_HEIGHT)).intValue()));
                } catch (MalformedURLException e) {
                    throw new JsonDeserializationException(e);
                }
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
