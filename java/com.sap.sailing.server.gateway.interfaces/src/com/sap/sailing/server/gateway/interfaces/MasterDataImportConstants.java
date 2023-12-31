package com.sap.sailing.server.gateway.interfaces;

public interface MasterDataImportConstants {
    String MASTER_DATA_RESOURCE_BASE_URL = "/v1/masterdata/leaderboardgroups";
    String QUERY_PARAM_UUIDS = "uuids[]";
    String QUERY_PARAM_COMPRESS = "compress";
    String QUERY_PARAM_EXPORT_WIND = "exportWind";
    String QUERY_PARAM_EXPORT_DEVICE_CONFIGS = "exportDeviceConfigs";
    String QUERY_PARAM_EXPORT_TRACKED_RACES_AND_START_TRACKING = "exportTrackedRacesAndStartTracking";
}
