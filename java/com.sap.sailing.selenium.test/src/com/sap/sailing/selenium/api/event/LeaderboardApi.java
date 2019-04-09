package com.sap.sailing.selenium.api.event;

import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class LeaderboardApi {

    private static final Logger logger = Logger.getLogger(LeaderboardApi.class.getName());

    private static final String LEADERBOARDS_V1_RESOURCE_URL = "/api/v1/leaderboards";
    private static final String LEADERBOARDS_V2_RESOURCE_URL = "/api/v2/leaderboards";
    private static final String LEADERBOARDS_LIST_URL = LEADERBOARDS_V2_RESOURCE_URL;
    private static final String LEADERBOARD_URL = LEADERBOARDS_V2_RESOURCE_URL + "/{leaderboardname}";
    private static final String START_DEVICE_MAPPING_URL = LEADERBOARDS_V1_RESOURCE_URL
            + "/{leaderboardname}/device_mappings/start";
    private static final String END_DEVICE_MAPPING_URL = LEADERBOARDS_V1_RESOURCE_URL
            + "/{leaderboardname}/device_mappings/end";

    public JSONArray getLeaderboards(ApiContext ctx) {
        return ctx.get(LEADERBOARDS_LIST_URL);
    }

    public JSONObject getLeaderboard(ApiContext ctx, String leaderboardName) {
        return ctx.get(LEADERBOARD_URL.replace("{leaderboardname}", leaderboardName));
    }

    public JSONObject deviceMappingsStart(ApiContext ctx, String leaderboardName, String competitorId, String boatId,
            String markId, String deviceUuid, String secret, Long fromMillis) {
        JSONObject json = new JSONObject();
        json.put("competitorId", competitorId);
        json.put("boatId", boatId);
        json.put("markId", markId);
        json.put("deviceUuid", deviceUuid);
        json.put("secret", secret);
        json.put("fromMillis", fromMillis);

        JSONObject result = ctx.post(START_DEVICE_MAPPING_URL.replace("{leaderboardname}", leaderboardName), null,
                json);
        logger.info("devicemapping started for : " + json.toJSONString());
        return result;
    }

    public JSONObject deviceMappingsEnd(ApiContext ctx, String leaderboardName, String competitorId, String boatId,
            String markId, String deviceUuid, String secret, Long fromMillis) {
        JSONObject json = new JSONObject();
        json.put("competitorId", competitorId);
        json.put("boatId", boatId);
        json.put("markId", markId);
        json.put("deviceUuid", deviceUuid);
        json.put("secret", secret);
        json.put("toMillis", fromMillis);

        JSONObject result = ctx.post(END_DEVICE_MAPPING_URL.replace("{leaderboardname}", leaderboardName), null, json);
        logger.info("devicemapping ended for : " + json.toJSONString());
        return result;
    }
}
