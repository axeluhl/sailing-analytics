package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;


public class ConfigurationSixtyInch {
    private String eventUidAsString;
    private String leaderboardName;

    public ConfigurationSixtyInch(String eventUidAsString, String leaderboardName) {
        this.eventUidAsString = eventUidAsString;
        this.leaderboardName = leaderboardName;
    }

    public String getEventUidAsString() {
        return eventUidAsString;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }
}
