package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
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
    
    private final EventBaseJsonSerializer eventBaseJsonSerializer;

    public LeaderboardSearchResultJsonSerializer(EventBaseJsonSerializer eventBaseJsonSerializer) {
        this.eventBaseJsonSerializer = eventBaseJsonSerializer;
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
        return result;
    }

    private BoatClass getBoatClassFromTrackedRaces(Leaderboard leaderboard) {
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            return trackedRace.getRace().getBoatClass();
        }
        return null;
    }
}
