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
        return ctx.get(toUrl(LEADERBOARD_URL, leaderboardName));
    }

    public DeviceMappingRequest createDeviceMappingRequest(final ApiContext ctx, final String leaderboardName) {
        return new DeviceMappingRequest(ctx, leaderboardName);
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

    private String toUrl(final String urlTemplate, final String leaderboardName) {
        return urlTemplate.replace("{leaderboardname}", leaderboardName);
    }

    public class DeviceMappingRequest {

        private final JSONObject requestBody = new JSONObject();
        private final ApiContext context;
        private final String leaderboardName;

        private DeviceMappingRequest(final ApiContext context, final String leaderboardName) {
            this.context = context;
            this.leaderboardName = leaderboardName;
        }

        public DeviceMappingRequest forCompetitor(final UUID competitorId) {
            requestBody.put("competitorId", competitorId != null ? competitorId.toString() : null);
            return this;
        }

        public DeviceMappingRequest forBoat(final UUID boatId) {
            requestBody.put("boatId", boatId != null ? boatId.toString() : null);
            return this;
        }

        public DeviceMappingRequest forMark(final UUID markId) {
            requestBody.put("markId", markId != null ? markId.toString() : null);
            return this;
        }

        public DeviceMappingRequest withDeviceUuid(final UUID deviceId) {
            requestBody.put("deviceUuid", deviceId != null ? deviceId.toString() : null);
            return this;
        }

        public DeviceMappingRequest withSecret(final String secret) {
            requestBody.put("secret", secret);
            return this;
        }

        public JSONObject startDeviceMapping(final Long fromMillis) {
            requestBody.put("fromMillis", fromMillis);
            return post(START_DEVICE_MAPPING_URL, "devicemapping started for : ");
        }

        public JSONObject endDeviceMapping(final Long toMillis) {
            requestBody.put("toMillis", toMillis);
            return post(END_DEVICE_MAPPING_URL, "devicemapping ended for : ");
        }

        private JSONObject post(final String urlTemplate, final String logMessage) {
            final JSONObject result = context.post(toUrl(urlTemplate, leaderboardName), null, requestBody);
            logger.info(logMessage + requestBody.toJSONString());
            return result;
        }

    }

}
