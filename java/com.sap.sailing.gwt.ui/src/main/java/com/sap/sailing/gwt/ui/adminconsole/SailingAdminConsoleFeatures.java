package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.shared.security.Permission;
import com.sap.sse.gwt.adminconsole.AdminConsoleFeatures;

public interface SailingAdminConsoleFeatures {
    final AdminConsoleFeatures MANAGE_EVENTS = new AdminConsoleFeatures("MANAGE_EVENTS", Permission.MANAGE_EVENTS);
    final AdminConsoleFeatures MANAGE_REGATTAS = new AdminConsoleFeatures("MANAGE_REGATTAS", Permission.MANAGE_REGATTAS);
    final AdminConsoleFeatures MANAGE_TRACKED_RACES = new AdminConsoleFeatures("MANAGE_TRACKED_RACES", Permission.MANAGE_TRACKED_RACES);
    final AdminConsoleFeatures SHOW_TRACKED_RACES = new AdminConsoleFeatures("SHOW_TRACKED_RACES", Permission.SHOW_TRACKED_RACES);
    final AdminConsoleFeatures MANAGE_RACELOG_TRACKING = new AdminConsoleFeatures("MANAGE_RACELOG_TRACKING", Permission.MANAGE_RACELOG_TRACKING);
    final AdminConsoleFeatures MANAGE_ALL_COMPETITORS = new AdminConsoleFeatures("MANAGE_ALL_COMPETITORS", Permission.MANAGE_ALL_COMPETITORS);
    final AdminConsoleFeatures MANAGE_COURSE_LAYOUT = new AdminConsoleFeatures("MANAGE_COURSE_LAYOUT", Permission.MANAGE_COURSE_LAYOUT);
    final AdminConsoleFeatures MANAGE_WIND = new AdminConsoleFeatures("MANAGE_WIND", Permission.MANAGE_WIND);
    final AdminConsoleFeatures MANAGE_IGTIMI_ACCOUNTS = new AdminConsoleFeatures("MANAGE_IGTIMI_ACCOUNTS", Permission.MANAGE_IGTIMI_ACCOUNTS);
    final AdminConsoleFeatures MANAGE_LEADERBOARDS = new AdminConsoleFeatures("MANAGE_LEADERBOARDS", Permission.MANAGE_LEADERBOARDS);
    final AdminConsoleFeatures MANAGE_LEADERBOARD_GROUPS = new AdminConsoleFeatures("MANAGE_LEADERBOARD_GROUPS", Permission.MANAGE_LEADERBOARD_GROUPS);
    final AdminConsoleFeatures MANAGE_RESULT_IMPORT_URLS = new AdminConsoleFeatures("MANAGE_RESULT_IMPORT_URLS", Permission.MANAGE_RESULT_IMPORT_URLS);
    final AdminConsoleFeatures MANAGE_STRUCTURE_IMPORT_URLS = new AdminConsoleFeatures("MANAGE_STRUCTURE_IMPORT_URLS", Permission.MANAGE_STRUCTURE_IMPORT_URLS);
    final AdminConsoleFeatures MANAGE_MEDIA = new AdminConsoleFeatures("MANAGE_MEDIA", Permission.MANAGE_MEDIA);
    final AdminConsoleFeatures MANAGE_SAILING_SERVER_INSTANCES = new AdminConsoleFeatures("MANAGE_SAILING_SERVER_INSTANCES", Permission.MANAGE_SAILING_SERVER_INSTANCES);
    final AdminConsoleFeatures MANAGE_REPLICATION = new AdminConsoleFeatures("MANAGE_REPLICATION", Permission.MANAGE_REPLICATION);
    final AdminConsoleFeatures MANAGE_MASTERDATA_IMPORT = new AdminConsoleFeatures("MANAGE_MASTERDATA_IMPORT", Permission.MANAGE_MASTERDATA_IMPORT);
    final AdminConsoleFeatures MANAGE_DEVICE_CONFIGURATION = new AdminConsoleFeatures("MANAGE_DEVICE_CONFIGURATION", Permission.MANAGE_DEVICE_CONFIGURATION);
    final AdminConsoleFeatures MANAGE_USERS = new AdminConsoleFeatures("MANAGE_USERS", Permission.MANAGE_USERS);
    final AdminConsoleFeatures MANAGE_FILE_STORAGE = new AdminConsoleFeatures("MANAGE_FILE_STORAGE", Permission.MANAGE_FILE_STORAGE);
}
