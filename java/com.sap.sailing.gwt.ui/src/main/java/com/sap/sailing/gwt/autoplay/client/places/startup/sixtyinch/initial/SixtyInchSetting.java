package com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial;

import java.util.UUID;

public class SixtyInchSetting {

    private UUID eventId;
    private String leaderBoardName;
    private boolean manualMode;

    public SixtyInchSetting(UUID eventUuid, String selectedLeaderboardName) {
        this.eventId = eventUuid;
        this.leaderBoardName = selectedLeaderboardName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getLeaderBoardName() {
        return leaderBoardName;
    }

    public boolean isManualMode() {
        return manualMode;
    }
}

