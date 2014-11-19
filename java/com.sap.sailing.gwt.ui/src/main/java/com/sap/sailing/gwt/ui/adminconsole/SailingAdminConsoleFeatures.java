package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.usermanagement.UserRoles;
import com.sap.sse.gwt.adminconsole.AdminConsoleFeatures;

public interface SailingAdminConsoleFeatures {
    final AdminConsoleFeatures MANAGE_EVENTS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_REGATTAS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_TRACKED_RACES = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures SHOW_TRACKED_RACES = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_RACELOG_TRACKING = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_ALL_COMPETITORS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_COURSE_LAYOUT = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_WIND = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_IGTIMI_ACCOUNTS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_LEADERBOARDS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_LEADERBOARD_GROUPS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_RESULT_IMPORT_URLS = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_MEDIA = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_SAILING_SERVER_INSTANCES = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_REPLICATION = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_MASTERDATA_IMPORT = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_DEVICE_CONFIGURATION = new AdminConsoleFeatures("MANAGE_EVENTS", UserRoles.eventmanager.getRolename(), UserRoles.administrator.getRolename());
    final AdminConsoleFeatures MANAGE_USERS = new AdminConsoleFeatures("MANAGE_USERS", UserRoles.administrator.getRolename());
}
