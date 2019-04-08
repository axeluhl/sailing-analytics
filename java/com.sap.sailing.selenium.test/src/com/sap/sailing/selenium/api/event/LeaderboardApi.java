package com.sap.sailing.selenium.api.event;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class LeaderboardApi {

    private static final String LEADERBOARDS_RESOURCE_URL = "/api/v2/leaderboards";
    private static final String LEADERBOARDS_LIST_URL = LEADERBOARDS_RESOURCE_URL;
    private static final String LEADERBOARD_URL = LEADERBOARDS_RESOURCE_URL + "/{leaderboardname}";
    private static final String START_DEVICE_MAPPING_URL = LEADERBOARD_URL + "/device_mappings/start";

    public JSONArray getLeaderboards(ApiContext ctx) {
        return ctx.getList(LEADERBOARDS_LIST_URL);
    }

    public JSONObject getLeaderboard(ApiContext ctx, String leaderboardName) {
        return ctx.get(LEADERBOARD_URL.replaceAll("{leaderboardname}", leaderboardName));
    }

    public JSONObject deviceMappingsStart(ApiContext ctx, String leaderboardName, String competitorId, String boatId,
            String markId, String deviceUuid, String secret, Long fromMillis) {
        StringBuilder requestObjectBuilder = new StringBuilder();
        requestObjectBuilder.append("{");
        requestObjectBuilder.append("'competitorId':").append(competitorId).append(",");
        requestObjectBuilder.append("'boatId':").append(boatId).append(",");
        requestObjectBuilder.append("'markId':").append(markId).append(",");
        requestObjectBuilder.append("'deviceUuid':").append(deviceUuid).append(",");
        requestObjectBuilder.append("'secret':").append(secret).append(",");
        requestObjectBuilder.append("'fromMillis':").append(fromMillis).append(",");
        requestObjectBuilder.append("}");

        return ctx.post(START_DEVICE_MAPPING_URL.replaceAll("{leaderboardname}", leaderboardName), null,
                requestObjectBuilder.toString());
    }
}
