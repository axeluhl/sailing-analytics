package com.sap.sailing.gwt.autoplay.client.app.classic;

import java.util.UUID;

public class ClassicSetting {

    private UUID eventId;
    private String leaderBoardName;
    private boolean manualMode;

    public ClassicSetting(UUID eventUuid, String selectedLeaderboardName) {
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

