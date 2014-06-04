package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeaderboardSearchResultJsonSerializer implements JsonSerializer<LeaderboardSearchResult> {
    public static final String FIELD_SERVER_BASE_URL = "serverBaseURL";
    public static final String FIELD_EVENT = "event";
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
        result.put(
                FIELD_EVENT,
                leaderboardSearchResult.getEvent() == null ? null : eventBaseJsonSerializer
                        .serialize(leaderboardSearchResult.getEvent()));
        JSONObject leaderboardJson = new JSONObject();
        result.put(FIELD_LEADERBOARD, leaderboardJson);
        Leaderboard leaderboard = leaderboardSearchResult.getLeaderboard();
        leaderboardJson.put(FIELD_LEADERBOARD_NAME, leaderboard.getName());
        leaderboardJson.put(FIELD_LEADERBOARD_DISPLAY_NAME, leaderboard.getDisplayName());
        final BoatClass boatClass;
        final Regatta regatta;
        if (leaderboard instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
            boatClass = regatta.getBoatClass();
        } else {
            regatta = null;
            boatClass = getBoatClassFromTrackedRaces(leaderboard);
        }
        leaderboardJson.put(FIELD_LEADERBOARD_BOAT_CLASS_NAME, boatClass == null ? null : boatClass.getName());
        leaderboardJson.put(FIELD_LEADERBOARD_REGATTA_NAME, regatta == null ? null : regatta.getName());
        JSONArray leaderboardGroupsJson = new JSONArray();
        leaderboardJson.put(FIELD_LEADERBOARD_IN_LEADERBOARD_GROUPS, leaderboardGroupsJson);
        for (LeaderboardGroup lg : leaderboardSearchResult.getLeaderboardGroups()) {
            leaderboardGroupsJson.add(leaderboardGroupBaseJsonSerializer.serialize(lg));
        }
        return result;
    }

    private BoatClass getBoatClassFromTrackedRaces(Leaderboard leaderboard) {
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            return trackedRace.getRace().getBoatClass();
        }
        return null;
    }
}
