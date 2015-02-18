package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.shared.security.Permission;
import com.sap.sse.gwt.adminconsole.AdminConsoleFeatures;

public interface SailingAdminConsoleFeatures {
    final AdminConsoleFeatures MANAGE_EVENTS = new AdminConsoleFeatures(Permission.MANAGE_EVENTS);
    final AdminConsoleFeatures MANAGE_REGATTAS = new AdminConsoleFeatures(Permission.MANAGE_REGATTAS);
    final AdminConsoleFeatures MANAGE_TRACKED_RACES = new AdminConsoleFeatures(Permission.MANAGE_TRACKED_RACES);
    final AdminConsoleFeatures SHOW_TRACKED_RACES = new AdminConsoleFeatures(Permission.SHOW_TRACKED_RACES);
    final AdminConsoleFeatures MANAGE_RACELOG_TRACKING = new AdminConsoleFeatures(Permission.MANAGE_RACELOG_TRACKING);
    final AdminConsoleFeatures MANAGE_ALL_COMPETITORS = new AdminConsoleFeatures(Permission.MANAGE_ALL_COMPETITORS);
    final AdminConsoleFeatures MANAGE_COURSE_LAYOUT = new AdminConsoleFeatures(Permission.MANAGE_COURSE_LAYOUT);
    final AdminConsoleFeatures MANAGE_WIND = new AdminConsoleFeatures(Permission.MANAGE_WIND);
    final AdminConsoleFeatures MANAGE_IGTIMI_ACCOUNTS = new AdminConsoleFeatures(Permission.MANAGE_IGTIMI_ACCOUNTS);
    final AdminConsoleFeatures MANAGE_LEADERBOARDS = new AdminConsoleFeatures(Permission.MANAGE_LEADERBOARDS);
    final AdminConsoleFeatures MANAGE_LEADERBOARD_GROUPS = new AdminConsoleFeatures(Permission.MANAGE_LEADERBOARD_GROUPS);
    final AdminConsoleFeatures MANAGE_RESULT_IMPORT_URLS = new AdminConsoleFeatures(Permission.MANAGE_RESULT_IMPORT_URLS);
    final AdminConsoleFeatures MANAGE_STRUCTURE_IMPORT_URLS = new AdminConsoleFeatures(Permission.MANAGE_STRUCTURE_IMPORT_URLS);
    final AdminConsoleFeatures MANAGE_MEDIA = new AdminConsoleFeatures(Permission.MANAGE_MEDIA);
    final AdminConsoleFeatures MANAGE_SAILING_SERVER_INSTANCES = new AdminConsoleFeatures(Permission.MANAGE_SAILING_SERVER_INSTANCES);
    final AdminConsoleFeatures MANAGE_REPLICATION = new AdminConsoleFeatures(Permission.MANAGE_REPLICATION);
    final AdminConsoleFeatures MANAGE_MASTERDATA_IMPORT = new AdminConsoleFeatures(Permission.MANAGE_MASTERDATA_IMPORT);
    final AdminConsoleFeatures MANAGE_DEVICE_CONFIGURATION = new AdminConsoleFeatures(Permission.MANAGE_DEVICE_CONFIGURATION);
    final AdminConsoleFeatures MANAGE_USERS = new AdminConsoleFeatures(Permission.MANAGE_USERS);
    final AdminConsoleFeatures MANAGE_FILE_STORAGE = new AdminConsoleFeatures(Permission.MANAGE_FILE_STORAGE);
}
