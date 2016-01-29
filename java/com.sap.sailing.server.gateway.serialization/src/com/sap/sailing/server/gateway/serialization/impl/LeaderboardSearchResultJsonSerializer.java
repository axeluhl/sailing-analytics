package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeaderboardSearchResultJsonSerializer implements JsonSerializer<LeaderboardSearchResult> {
    public static final String FIELD_SERVER_BASE_URL = "serverBaseURL";
    public static final String FIELD_EVENTS = "events";
    public static final String FIELD_LEADERBOARD = "leaderboard";
    public static final String FIELD_LEADERBOARD_NAME = "name";
    public static final String FIELD_LEADERBOARD_DISPLAY_NAME = "displayName";
    public static final String FIELD_LEADERBOARD_BOAT_CLASS_NAME = "boatClassName";
    public static final String FIELD_LEADERBOARD_REGATTA_NAME = "regattaName";
    public static final String FIELD_LEADERBOARD_IN_LEADERBOARD_GROUPS = "inLeaderboardGroups";
    
    private final JsonSerializer<EventBase> eventBaseJsonSerializer;
    private final JsonSerializer<LeaderboardGroupBase> leaderboardGroupBaseJsonSerializer;

    public LeaderboardSearchResultJsonSerializer(JsonSerializer<EventBase> eventBaseJsonSerializer, JsonSerializer<LeaderboardGroupBase> leaderboardGroupBaseJsonSerializer) {
        this.eventBaseJsonSerializer = eventBaseJsonSerializer;
        this.leaderboardGroupBaseJsonSerializer = leaderboardGroupBaseJsonSerializer;
    }

    @Override
    public JSONObject serialize(LeaderboardSearchResult leaderboardSearchResult) {
        JSONObject result = new JSONObject();
        JSONArray eventsJson = new JSONArray();
        for (final Event e : leaderboardSearchResult.getEvents()) {
            eventsJson.add(eventBaseJsonSerializer.serialize(e));
        }
        result.put(FIELD_EVENTS, eventsJson);
        JSONObject leaderboardJson = new JSONObject();
        result.put(FIELD_LEADERBOARD, leaderboardJson);
        Leaderboard leaderboard = leaderboardSearchResult.getLeaderboard();
        leaderboardJson.put(FIELD_LEADERBOARD_NAME, leaderboard.getName());
        leaderboardJson.put(FIELD_LEADERBOARD_DISPLAY_NAME, leaderboard.getDisplayName());
        final String boatClassName;
        final Regatta regatta;
        if (leaderboard instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
            boatClassName = regatta.getBoatClass().getName();
        } else {
            regatta = null;
            boatClassName = leaderboardSearchResult.getBoatClassName();
        }
        leaderboardJson.put(FIELD_LEADERBOARD_BOAT_CLASS_NAME, boatClassName);
        leaderboardJson.put(FIELD_LEADERBOARD_REGATTA_NAME, regatta == null ? null : regatta.getName());
        JSONArray leaderboardGroupsJson = new JSONArray();
        leaderboardJson.put(FIELD_LEADERBOARD_IN_LEADERBOARD_GROUPS, leaderboardGroupsJson);
        for (LeaderboardGroup lg : leaderboardSearchResult.getLeaderboardGroups()) {
            leaderboardGroupsJson.add(leaderboardGroupBaseJsonSerializer.serialize(lg));
        }
        return result;
    }
}
