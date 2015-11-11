package com.sap.sailing.dashboards.gwt.shared;

import com.google.gwt.user.client.Window;


/**
 * @author Alexander Ries (D062114)
 *
 */
public enum DashboardURLParameters {

    EVENT_ID("eventId"),
    LEADERBOARD_NAME("leaderboardName");
    
    private String name;

    private DashboardURLParameters(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getValue() {
        return Window.Location.getParameter(getName());
    }
}
