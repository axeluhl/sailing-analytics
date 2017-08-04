package com.sap.sailing.domain.anniversary;

import java.util.Date;
import java.util.UUID;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class AnniversaryRaceInfo extends SimpleAnniversaryRaceInfo {
    public AnniversaryRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, Date startOfRace,
            UUID eventId) {
        super(identifier, startOfRace);
        if (leaderboardName == null || eventId == null) {
            throw new IllegalStateException("Anniversary Data is not allowed to contain any null values!");
        }
        this.identifier = identifier;
        this.leaderboardName = leaderboardName;
        this.startOfRace = startOfRace;
        this.eventID = eventId;
    }

    String leaderboardName;
    UUID eventID;

    public RegattaAndRaceIdentifier getIdentifier() {
        return identifier;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public Date getStartOfRace() {
        return startOfRace;
    }

    public UUID getEventID() {
        return eventID;
    }

    @Override
    public String toString() {
        return "AnniversaryRaceInfo [identifier=" + identifier + ", leaderboardName=" + leaderboardName
                + ", startOfRace=" + startOfRace + ", eventID=" + eventID + "]";
    }


    
    
}
