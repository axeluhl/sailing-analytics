package com.sap.sailing.selenium.api.event;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

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
    private static final String UPDATE_LEADERBOARD_URL = LEADERBOARDS_V1_RESOURCE_URL + "/{leaderboardname}/update";
    
    private static final String PARAM_RESULT_DISCARDING_THRESHOLDS = "resultDiscardingThresholds";

    public JSONArray getLeaderboards(ApiContext ctx) {
        return ctx.get(LEADERBOARDS_LIST_URL);
    }

    public Leaderboard getLeaderboard(ApiContext ctx, String leaderboardName) {
        return new Leaderboard(ctx.get(toUrl(LEADERBOARD_URL, leaderboardName)));
    }

    public DeviceMappingRequest createDeviceMappingRequest(final ApiContext ctx, final String leaderboardName) {
        return new DeviceMappingRequest(ctx, leaderboardName);
    }

    public void updateLeaderboard(ApiContext ctx, String leaderboardName, int[] resultDiscardingThresholds) {
        JSONObject requestJson = new JSONObject();
        JSONArray resultDiscardingThresholdsJsonArray = new JSONArray();
        if (resultDiscardingThresholds != null) {
            for (int i = 0; i < resultDiscardingThresholds.length; i++) {
                resultDiscardingThresholdsJsonArray.add(resultDiscardingThresholds[i]);
            }
        }
        requestJson.put(PARAM_RESULT_DISCARDING_THRESHOLDS,
                resultDiscardingThresholds != null ? resultDiscardingThresholdsJsonArray : null);
        System.out.println(requestJson.toJSONString());
        ctx.post(toUrl(UPDATE_LEADERBOARD_URL, leaderboardName), null, requestJson);
    }

    public TrackingTimes setTrackingTimes(ApiContext ctx, String leaderboardName, String raceColumnName,
            String fleetName, Long startTime, Long endTime) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("race_column", raceColumnName);
        queryParams.put("fleet", fleetName);
        queryParams.put("startoftrackingasmillis", startTime != null ? startTime.toString() : null);
        queryParams.put("endoftrackingasmillis", endTime != null ? endTime.toString() : null);
        return new TrackingTimes(ctx.post(toUrl(SET_TRACKING_TIMES_URL, leaderboardName), queryParams));
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

    public class Leaderboard extends JsonWrapper {

        public Leaderboard(JSONObject json) {
            super(json);
        }

        public String getName() {
            return get("name");
        }

        public String getDisplayName() {
            return get("displayName");
        }

        public Long getResultTimePoint() {
            return get("resultTimepoint");
        }

        public String getResultState() {
            return get("resultState");
        }

        public String getType() {
            return get("type");
        }

        public String getShardingLeaderboardName() {
            return get("shardingLeaderboardName");
        }

        public int[] getDiscardIndexResultsStartingWithHowManyRaces() {
            final JSONArray resultDiscardingThresholdsRaw = (JSONArray) get(
                    "discardIndexResultsStartingWithHowManyRaces");
            final int[] resultDiscardingThresholds = new int[resultDiscardingThresholdsRaw.size()];
            for (int i = 0; i < resultDiscardingThresholdsRaw.size(); i++) {
                resultDiscardingThresholds[i] = ((Long) resultDiscardingThresholdsRaw.get(i)).intValue();
            }
            return resultDiscardingThresholds;
        }

        public Boolean canBoatsOfCompetitorsChangePerRace() {
            return get("canBoatsOfCompetitorsChangePerRace");
        }

        public Long getMaxCompetitorsCount() {
            return get("maxCompetitorsCount");
        }

        public String getScoringComment() {
            return get("scoringComment");
        }

        public Long getLastScoringUpdate() {
            return get("lastScoringUpdate");
        }

        public String[] getColumnNames() {
            final JSONArray columnNamesRaw = (JSONArray) get("columnNames");
            final String[] columnNames = new String[columnNamesRaw.size()];
            for (int i = 0; i < columnNamesRaw.size(); i++) {
                columnNames[i] = (String) columnNamesRaw.get(i);
            }
            return columnNames;
        }

        public JSONArray getCompetitors() {
            return get("competitors");
        }

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

    public class TrackingTimes extends JsonWrapper {

        private TrackingTimes(JSONObject json) {
            super(json);
        }

        public Long getStartOfTracking() {
            return get("startoftracking");
        }

        public Long getEndOfTracking() {
            return get("endoftracking");
        }
    }

}
