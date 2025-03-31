package com.sap.sailing.dashboards.gwt.shared;

import com.google.gwt.user.client.Window;


/**
 * @author Alexander Ries (D062114)
 *
 */
public enum DashboardURLParameters {

    EVENT_ID("eventId"),
    LEADERBOARD_NAME("leaderboardName");
    
    private String key;

    private DashboardURLParameters(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return Window.Location.getParameter(getKey());
    }
}
