package com.sap.sailing.selenium.api.event;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
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
    private static final String STOP_TRACKING_URL = LEADERBOARDS_V1_RESOURCE_URL + "/{leaderboardname}/stoptracking";
    private static final String UPDATE_LEADERBOARD_URL = LEADERBOARDS_V1_RESOURCE_URL + "/{leaderboardname}/update";
    private static final String PARAM_RESULT_DISCARDING_THRESHOLDS = "resultDiscardingThresholds";
    private static final String PARAM_LEADERBOARD_DISPLAY_NAME = "leaderboardDisplayName";
    private static final String GET_MARK_URL = LEADERBOARDS_V1_RESOURCE_URL + "/{leaderboardname}/marks/";
    private static final String START_TIME_URL = LEADERBOARDS_V1_RESOURCE_URL + "/{leaderboardname}/starttime";
    private static final String PARAM_RACE_COLUMN_NAME = "race_column";
    private static final String PARAM_RACE_FLEET_NAME = "fleet";
    private static final String PARAM_FIELD_AUTHOR_NAME = "authorName";
    private static final String PARAM_FIELD_AUTHOR_PRIORITY = "authorPriority";
    private static final String PARAM_START_TIME = "startTime";
    private static final String PARAM_START_PROCEDURE_TYPE = "startProcedureType";
    private static final String PARAM_PASS_ID = "passId";
    private static final String PARAM_START_OF_TRACKING = "startoftrackingasmillis";
    private static final String PARAM_END_OF_TRACKING = "endoftrackingasmillis";
    private static final String PARAM_TRACK_WIND = "trackWind";
    private static final String PARAM_CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION = "correctWindDirectionByMagneticDeclination";

    public JSONArray getLeaderboards(final ApiContext ctx) {
        return ctx.get(LEADERBOARDS_LIST_URL);
    }

    public Leaderboard getLeaderboard(ApiContext ctx, String leaderboardName) {
        return new Leaderboard(ctx.get(toUrl(LEADERBOARD_URL, leaderboardName)));
    }

    public DeviceMappingRequest createDeviceMappingRequest(final ApiContext ctx, final String leaderboardName) {
        return new DeviceMappingRequest(ctx, leaderboardName);
    }

    public void updateLeaderboard(ApiContext ctx, String leaderboardName, String leaderboardDisplayName,
            int[] resultDiscardingThresholds) {
        JSONObject requestJson = new JSONObject();
        JSONArray resultDiscardingThresholdsJsonArray = new JSONArray();
        if (resultDiscardingThresholds != null) {
            for (int i = 0; i < resultDiscardingThresholds.length; i++) {
                resultDiscardingThresholdsJsonArray.add(resultDiscardingThresholds[i]);
            }
        }
        requestJson.put(PARAM_RESULT_DISCARDING_THRESHOLDS,
                resultDiscardingThresholds != null ? resultDiscardingThresholdsJsonArray : null);
        requestJson.put(PARAM_LEADERBOARD_DISPLAY_NAME, leaderboardDisplayName);
        ctx.post(toUrl(UPDATE_LEADERBOARD_URL, leaderboardName), null, requestJson);
    }

    public void updateLeaderboardDisplayName(ApiContext ctx, String leaderboardName, String leaderboardDisplayName) {
        JSONObject requestJson = new JSONObject();
        requestJson.put(PARAM_LEADERBOARD_DISPLAY_NAME, leaderboardDisplayName);
        ctx.post(toUrl(UPDATE_LEADERBOARD_URL, leaderboardName), null, requestJson);
    }

    public void updateLeaderboardResultDiscardingThresholds(ApiContext ctx, String leaderboardName,
            int[] resultDiscardingThresholds) {
        JSONObject requestJson = new JSONObject();
        JSONArray resultDiscardingThresholdsJsonArray = new JSONArray();
        if (resultDiscardingThresholds != null) {
            for (int i = 0; i < resultDiscardingThresholds.length; i++) {
                resultDiscardingThresholdsJsonArray.add(resultDiscardingThresholds[i]);
            }
        }
        requestJson.put(PARAM_RESULT_DISCARDING_THRESHOLDS,
                resultDiscardingThresholds != null ? resultDiscardingThresholdsJsonArray : null);
        ctx.post(toUrl(UPDATE_LEADERBOARD_URL, leaderboardName), null, requestJson);
    }

    public TrackingTimes setTrackingTimes(final ApiContext ctx, final String leaderboardName,
            final String raceColumnName, String fleetName, Long startTime, Long endTime) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_RACE_COLUMN_NAME, raceColumnName);
        queryParams.put(PARAM_RACE_FLEET_NAME, fleetName);
        queryParams.put(PARAM_START_OF_TRACKING, startTime != null ? startTime.toString() : null);
        queryParams.put(PARAM_END_OF_TRACKING, endTime != null ? endTime.toString() : null);
        return new TrackingTimes(ctx.post(toUrl(SET_TRACKING_TIMES_URL, leaderboardName), queryParams));
    }

    public JSONObject startRaceLogTracking(ApiContext ctx, String leaderboardName, String raceColumnName,
            String fleetName) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_RACE_COLUMN_NAME, raceColumnName);
        queryParams.put(PARAM_RACE_FLEET_NAME, fleetName);
        queryParams.put(PARAM_TRACK_WIND, Boolean.FALSE.toString());
        queryParams.put(PARAM_CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION, Boolean.FALSE.toString());
        return ctx.post(toUrl(START_TRACKING_URL, leaderboardName), queryParams);
    }

    public JSONArray stopRaceLogTracking(ApiContext ctx, String leaderboardName, String raceColumnName,
            String fleetName) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_RACE_COLUMN_NAME, raceColumnName);
        queryParams.put(PARAM_RACE_FLEET_NAME, fleetName);
        return ctx.post(toUrl(STOP_TRACKING_URL, leaderboardName), queryParams);
    }

    public StartTime getStartTime(final ApiContext ctx, final String leaderboardName, final String raceColumnName,
            final String fleetName) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_RACE_COLUMN_NAME, raceColumnName);
        queryParams.put(PARAM_RACE_FLEET_NAME, fleetName);
        return new StartTime(ctx.get(toUrl(START_TIME_URL, leaderboardName), queryParams));
    }

    public Long setStartTime(final ApiContext ctx, final String leaderboardName, final String raceColumnName,
            final String fleetName, final Long startTime, final Integer passId,
            final RacingProcedureType startProcedureType, final String authorName, final Integer authorPriortity) {
        final Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(PARAM_RACE_COLUMN_NAME, raceColumnName);
        queryParams.put(PARAM_RACE_FLEET_NAME, fleetName);
        queryParams.put(PARAM_FIELD_AUTHOR_NAME, authorName);
        queryParams.put(PARAM_FIELD_AUTHOR_PRIORITY, authorPriortity != null ? authorPriortity.toString() : null);
        queryParams.put(PARAM_START_TIME, startTime != null ? startTime.toString() : null);
        queryParams.put(PARAM_START_PROCEDURE_TYPE, startProcedureType != null ? startProcedureType.name() : null);
        queryParams.put(PARAM_PASS_ID, passId != null ? passId.toString() : null);
        final JSONObject result = ctx.put(toUrl(START_TIME_URL, leaderboardName), queryParams,
                new HashMap<String, String>());
        return (Long) result.get(StartTime.START_TIME_AS_MILLIS);
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

    public Mark getMark(ApiContext ctx, String leaderboardName, UUID markUUID) {
        return new Mark(ctx.get(toUrl(GET_MARK_URL, leaderboardName) + markUUID));
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

        public void startDeviceMapping(final Long fromMillis) {
            requestBody.put("fromMillis", fromMillis);
            post(START_DEVICE_MAPPING_URL, "devicemapping started for : ");
        }

        public void endDeviceMapping(final Long toMillis) {
            requestBody.put("toMillis", toMillis);
            post(END_DEVICE_MAPPING_URL, "devicemapping ended for : ");
        }

        private JSONObject post(final String urlTemplate, final String logMessage) {
            final JSONObject result = context.post(toUrl(urlTemplate, leaderboardName), null, requestBody);
            logger.info(logMessage + requestBody.toJSONString());
            return result;
        }

    }

    public class TrackingTimes extends JsonWrapper {

        private static final String START_OF_TRACKING = "startoftracking";
        private static final String END_OF_TRACKING = "endoftracking";

        private TrackingTimes(JSONObject json) {
            super(json);
        }

        public Long getStartOfTracking() {
            return get(START_OF_TRACKING);
        }

        public Long getEndOfTracking() {
            return get(END_OF_TRACKING);
        }
    }

    public class StartTime extends JsonWrapper {

        private static final String START_TIME_AS_MILLIS = "startTimeAsMillis";
        private static final String PASS_ID = "passId";
        private static final String RACING_PROCEDURE_TYPE = "racingProcedureType";

        private StartTime(final JSONObject json) {
            super(json);
        }

        public Long getStartTimeAsMillis() {
            return get(START_TIME_AS_MILLIS);
        }

        public Integer getPassId() {
            Long passID = (Long) get(PASS_ID);
            return passID != null ? passID.intValue() : null;
        }

        public RacingProcedureType getRacingProcedureType() {
            return RacingProcedureType.valueOf(get(RACING_PROCEDURE_TYPE));
        }
    }

}
