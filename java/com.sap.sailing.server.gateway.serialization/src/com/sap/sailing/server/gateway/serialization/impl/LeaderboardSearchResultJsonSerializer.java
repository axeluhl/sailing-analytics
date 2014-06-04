package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeaderboardSearchResultJsonSerializer implements JsonSerializer<LeaderboardSearchResult> {
    public static final String FIELD_SERVER_BASE_URL = "serverBaseURL";
    public static final String FIELD_EVENT = "event";
    public static final String FIELD_LEADERBOARD = "leaderboard";
    
    private final EventBaseJsonSerializer eventBaseJsonSerializer;

    public LeaderboardSearchResultJsonSerializer(EventBaseJsonSerializer eventBaseJsonSerializer) {
        this.eventBaseJsonSerializer = eventBaseJsonSerializer;
    }

    @Override
    public JSONObject serialize(LeaderboardSearchResult leaderboardSearchResult) {
        JSONObject result = new JSONObject();
        result.put(FIELD_EVENT, eventBaseJsonSerializer.serialize(leaderboardSearchResult.getEvent()));
        return null;
    }
}
