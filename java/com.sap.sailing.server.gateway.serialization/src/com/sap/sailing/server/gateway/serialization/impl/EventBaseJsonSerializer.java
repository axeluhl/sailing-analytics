package com.sap.sailing.server.gateway.serialization.impl;

import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class EventBaseJsonSerializer implements JsonSerializer<EventBase> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_VENUE = "venue";
    public static final String FIELD_IMAGE_URLS = "imageURLs";
    public static final String FIELD_VIDEO_URLS = "videoURLs";
    public static final String FIELDS_LEADERBOARD_GROUPS = "leaderboardGroups";

    private final JsonSerializer<Venue> venueSerializer;
    private final JsonSerializer<? super LeaderboardGroupBase> leaderboardGroupSerializer;

    public EventBaseJsonSerializer(JsonSerializer<Venue> venueSerializer, JsonSerializer<? super LeaderboardGroupBase> leaderboardGroupSerializer) {
        this.leaderboardGroupSerializer = leaderboardGroupSerializer;
        this.venueSerializer = venueSerializer;
    }

    public JSONObject serialize(EventBase event) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, event.getId().toString());
        result.put(FIELD_NAME, event.getName());
        result.put(FIELD_START_DATE, event.getStartDate() != null ? event.getStartDate().asMillis() : null);
        result.put(FIELD_END_DATE, event.getStartDate() != null ? event.getEndDate().asMillis() : null);
        result.put(FIELD_VENUE, venueSerializer.serialize(event.getVenue()));
        result.put(FIELD_IMAGE_URLS, getURLsAsStringArray(event.getImageURLs()));
        result.put(FIELD_VIDEO_URLS, getURLsAsStringArray(event.getVideoURLs()));
        JSONArray leaderboardGroups = new JSONArray();
        result.put(FIELDS_LEADERBOARD_GROUPS, leaderboardGroups);
        for (LeaderboardGroupBase lg : event.getLeaderboardGroups()) {
            leaderboardGroups.add(leaderboardGroupSerializer.serialize(lg));
        }
        return result;
    }

    private JSONArray getURLsAsStringArray(Iterable<URL> urls) {
        JSONArray jsonImageURLs = new JSONArray();
        for (URL url : urls) {
            jsonImageURLs.add(url.toString());
        }
        return jsonImageURLs;
    }
}
