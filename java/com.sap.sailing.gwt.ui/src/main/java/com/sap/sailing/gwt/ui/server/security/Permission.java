package com.sap.sailing.gwt.ui.server.security;

public enum Permission implements com.sap.sse.security.shared.Permission {
    MANAGE_EVENTS,
    MANAGE_REGATTAS,
    MANAGE_TRACKED_RACES,
    SHOW_TRACKED_RACES,
    MANAGE_RACELOG_TRACKING,
    MANAGE_ALL_COMPETITORS,
    MANAGE_COURSE_LAYOUT,
    MANAGE_WIND,
    MANAGE_IGTIMI_ACCOUNTS,
    MANAGE_LEADERBOARDS,
    MANAGE_LEADERBOARD_GROUPS,
    MANAGE_RESULT_IMPORT_URLS,
    MANAGE_STRUCTURE_IMPORT_URLS,
    MANAGE_MEDIA,
    MANAGE_SAILING_SERVER_INSTANCES,
    MANAGE_REPLICATION,
    MANAGE_MASTERDATA_IMPORT,
    MANAGE_DEVICE_CONFIGURATION,
    MANAGE_USERS,
    MANAGE_FILE_STORAGE;

    @Override
    public String getStringPermission() {
        return name()+":";
    }
}
