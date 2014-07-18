package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.usermanagement.UserRoles;

public enum AdminConsoleFeatures {
    
    MANAGE_EVENTS (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_REGATTAS (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_TRACKED_RACES (UserRoles.eventmanager, UserRoles.administrator),
    SHOW_TRACKED_RACES (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_RACELOG_TRACKING (UserRoles.administrator),
    MANAGE_ALL_COMPETITORS (UserRoles.administrator),
    MANAGE_COURSE_LAYOUT (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_WIND (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_IGTIMI_ACCOUNTS (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_LEADERBOARDS (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_LEADERBOARD_GROUPS (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_RESULT_IMPORT_URLS (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_MEDIA (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_SAILING_SERVER_INSTANCES (UserRoles.administrator),
    MANAGE_REPLICATION (UserRoles.administrator),
    MANAGE_MASTERDATA_IMPORT (UserRoles.administrator),
    MANAGE_DEVICE_CONFIGURATION (UserRoles.eventmanager, UserRoles.administrator),
    MANAGE_MARKPASSINGS (UserRoles.eventmanager, UserRoles.administrator);
    
    private UserRoles[] enabledRoles;
    
    AdminConsoleFeatures(UserRoles... enabledRoles) {
        this.enabledRoles = enabledRoles;
    }

    public UserRoles[] getEnabledRoles() {
        return enabledRoles;
    }
}
