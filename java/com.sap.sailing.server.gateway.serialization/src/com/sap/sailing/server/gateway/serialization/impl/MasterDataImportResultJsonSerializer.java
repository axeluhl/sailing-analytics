package com.sap.sailing.server.gateway.serialization.impl;

import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sse.shared.json.JsonSerializer;

public class MasterDataImportResultJsonSerializer implements JsonSerializer<MasterDataImportResult> {
    public static final String LEADERBOARDGROUPS_IMPORTED = "leaderboardgroupsImported";
    public static final String IMPORTED_FROM = "importedFrom";
    public static final String OVERRIDE_FORM_PARAM = "override";
    public static final String EXPORT_WIND_FORM_PARAM = "exportWind";
    public static final String COMPRESS_FORM_PARAM = "compress";
    public static final String LEADERBOARDGROUP_UUID_FORM_PARAM = "leaderboardgroupUUID[]";
    public static final String EXPORT_DEVICE_CONFIGS_FORM_PARAM = "exportDeviceConfigs";
    public static final String EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM = "exportTrackedRacesAndStartTracking";

    @Override
    public JSONObject serialize(MasterDataImportResult object) {
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(LEADERBOARDGROUPS_IMPORTED, getLeaderboardGroupNamesFromIdList(object.getLeaderboardGroupsImported()));
        jsonResponse.put(IMPORTED_FROM, object.getRemoteServerUrl());
        jsonResponse.put(OVERRIDE_FORM_PARAM, object.isOverride());
        jsonResponse.put(EXPORT_WIND_FORM_PARAM, object.isImportWind());
        jsonResponse.put(EXPORT_DEVICE_CONFIGS_FORM_PARAM, object.isImportDeviceConfigurations());
        jsonResponse.put(EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM, object.isImportTrackedRacesAndStartTracking());
        return jsonResponse;
    }

    private JSONArray getLeaderboardGroupNamesFromIdList(Iterable<MasterDataImportResult.LeaderboardGroupWithEventIds> leaderboardGroupsWithEventIds) {
        final JSONArray result = new JSONArray();
        for (final MasterDataImportResult.LeaderboardGroupWithEventIds leaderboardGroupWithEventIds : leaderboardGroupsWithEventIds) {
            final JSONObject lgJson = new JSONObject();
            result.add(lgJson);
            lgJson.put(LeaderboardGroupConstants.ID, leaderboardGroupWithEventIds.getId().toString());
            lgJson.put(LeaderboardGroupConstants.NAME, leaderboardGroupWithEventIds.getName());
            final JSONArray eventIds = new JSONArray();
            for (final UUID eventId : leaderboardGroupWithEventIds.getEventIds()) {
                eventIds.add(eventId.toString());
            }
            lgJson.put(LeaderboardGroupConstants.EVENTS, eventIds);
        }
        return result;
    }
}
