package com.sap.sailing.domain.common.dto;

public final class ExpeditionAllInOneConstants {
    public static final String REQUEST_PARAMETER_IMPORT_MODE = "importMode";
    public static final String REQUEST_PARAMETER_BOAT_CLASS = "boatClass";
    public static final String REQUEST_PARAMETER_REGATTA_NAME = "regattaName";
    public static final String REQUEST_PARAMETER_LOCALE = "locale";
    public static final String REQUEST_PARAMETER_IMPORT_START_DATA = "importStartData";
    public static final String RESPONSE_START_TIMES = "startTimes";
    public static final String RESPONSE_EVENT_ID = "eventId";
    public static final String RESPONSE_LEADER_BOARD_NAME = "leaderboardName";
    public static final String RESPONSE_LEADER_BOARD_GROUP_NAME = "leaderboardGroupName";
    public static final String RESPONSE_REGATTA_NAME = "regattaName";
    public static final String RESPONSE_RACE_LIST = "raceNameRaceColumnNameFleetNameList";
    public static final String RESPONSE_ERRORS = "errors";
    public static final String RESPONSE_GPS_DEVICE_IDS = "gpsDeviceIds";
    public static final String RESPONSE_SENSOR_DEVICE_IDS = "sensorDeviceIds";
    public static final String RESPONSE_SENSOR_FIX_IMPORTER_TYPE = "sensorFixImporterType";

    public enum ImportMode {
        NEW_COMPETITOR,
        NEW_EVENT,
        NEW_RACE;
    }

    private ExpeditionAllInOneConstants() {
    }
}
