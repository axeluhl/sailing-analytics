package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start;

import java.util.UUID;

public class SixtyInchSetting {

    private UUID eventId;
    private String leaderBoardName;

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
}

