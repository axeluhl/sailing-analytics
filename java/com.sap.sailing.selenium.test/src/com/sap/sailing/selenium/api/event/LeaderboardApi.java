package com.sap.sailing.selenium.api.event;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class LeaderboardApi {

    private static final String LIST_LEADERBOARDS = "/api/v2/leaderboards";

    public JSONObject getLeaderboard(ApiContext ctx, String leaderboardName) {
        return ctx.get(LIST_LEADERBOARDS + "/" + leaderboardName);
    }
}
