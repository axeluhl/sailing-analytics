package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

public class SlideContextImpl implements SlideContext {
    private String eventUidAsString;
    private String leaderboardName;

    public SlideContextImpl(String eventUidAsString, String leaderboardName) {
        this.eventUidAsString = eventUidAsString;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public String getEventUidAsString() {
        return eventUidAsString;
    }

    @Override
    public String getLeaderboardName() {
        return leaderboardName;
    }
}
