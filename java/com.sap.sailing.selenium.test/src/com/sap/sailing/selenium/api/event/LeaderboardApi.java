package com.sap.sailing.selenium.api.event;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
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
    private static final String SET_TRACKING_TIMES_URL = LEADERBOARDS_V1_RESOURCE_URL
            + "/{leaderboardname}/settrackingtimes";
    private static final String START_TRACKING_URL = LEADERBOARDS_V1_RESOURCE_URL + "/{leaderboardname}/starttracking";

    public JSONArray getLeaderboards(ApiContext ctx) {
        return ctx.get(LEADERBOARDS_LIST_URL);
    }

    public JSONObject getLeaderboard(ApiContext ctx, String leaderboardName) {
        return ctx.get(LEADERBOARD_URL.replace("{leaderboardname}", leaderboardName));
    }

    public JSONObject deviceMappingsStart(ApiContext ctx, String leaderboardName, UUID competitorId, UUID boatId,
            UUID markId, UUID deviceUuid, String secret, Long fromMillis) {
        final JSONObject json = initDeviceMappingRequestBody(competitorId, boatId, markId, deviceUuid, secret);
        json.put("fromMillis", fromMillis);

        final JSONObject result = ctx.post(toUrl(START_DEVICE_MAPPING_URL, leaderboardName), null, json);
        logger.info("devicemapping started for : " + json.toJSONString());
        return result;
    }

    public JSONObject deviceMappingsEnd(ApiContext ctx, String leaderboardName, UUID competitorId, UUID boatId,
            UUID markId, UUID deviceUuid, String secret, Long fromMillis) {
        final JSONObject json = initDeviceMappingRequestBody(competitorId, boatId, markId, deviceUuid, secret);
        json.put("toMillis", fromMillis);

        final JSONObject result = ctx.post(toUrl(END_DEVICE_MAPPING_URL, leaderboardName), null, json);
        logger.info("devicemapping ended for : " + json.toJSONString());
        return result;
    }

    public JSONObject setTrackingTimes(ApiContext ctx, String leaderboardName, String raceColumnName, String fleetName,
            Long startTime, Long endTime) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("race_column", raceColumnName);
        queryParams.put("fleet", fleetName);
        queryParams.put("startoftrackingasmillis", startTime != null ? startTime.toString() : null);
        queryParams.put("endoftrackingasmillis", endTime != null ? endTime.toString() : null);
        return ctx.post(toUrl(SET_TRACKING_TIMES_URL, leaderboardName), queryParams);
    }

    public JSONObject startRaceLogTracking(ApiContext ctx, String leaderboardName, String raceColumnName,
            String fleetName) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("race_column", raceColumnName);
        queryParams.put("fleet", fleetName);
        queryParams.put("trackWind", Boolean.FALSE.toString());
        queryParams.put("correctWindDirectionByMagneticDeclination", Boolean.FALSE.toString());
        return ctx.post(toUrl(START_TRACKING_URL, leaderboardName), queryParams);
    }

    private JSONObject initDeviceMappingRequestBody(final UUID competitorId, final UUID boatId, final UUID markId,
            final UUID deviceUuid, final String secret) {
        final JSONObject json = new JSONObject();
        json.put("competitorId", competitorId != null ? competitorId.toString() : null);
        json.put("boatId", boatId != null ? boatId.toString() : null);
        json.put("markId", markId != null ? markId.toString() : null);
        json.put("deviceUuid", deviceUuid != null ? deviceUuid.toString() : null);
        json.put("secret", secret);
        return json;
    }

    private String toUrl(final String urlTemplate, final String leaderboardName) {
        return urlTemplate.replace("{leaderboardname}", leaderboardName);
    }
}
