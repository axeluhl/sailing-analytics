package com.sap.sailing.domain.anniversary;

import java.util.Date;
import java.util.UUID;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class DetailedRaceInfo extends SimpleRaceInfo {
    private final String leaderboardName;
    private final UUID eventID;
    
    public DetailedRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, Date startOfRace,
            UUID eventId, String remoteName) {
        super(identifier, startOfRace, remoteName);
        if (leaderboardName == null || eventId == null) {
            throw new IllegalStateException("DetailedRaceInfo Data is not allowed to contain any null values!");
        }
        this.leaderboardName = leaderboardName;
        this.eventID = eventId;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public UUID getEventID() {
        return eventID;
    }

    @Override
    public String toString() {
        return "DetailedRaceInfo [identifier=" + getIdentifier() + ", leaderboardName=" + leaderboardName
                + ", startOfRace=" + getStartOfRace() + ", eventID=" + eventID + "]";
    }
}
