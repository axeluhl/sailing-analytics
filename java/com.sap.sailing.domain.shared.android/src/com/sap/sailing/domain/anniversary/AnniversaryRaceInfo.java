package com.sap.sailing.domain.anniversary;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class AnniversaryRaceInfo extends SimpleAnniversaryRaceInfo {
    public AnniversaryRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, Date startOfRace,
            String eventID) {
        super(identifier, startOfRace);
        if (leaderboardName == null || eventID == null) {
            throw new IllegalStateException("Anniversary Data is not allowed to contain any null values!");
        }
        this.identifier = identifier;
        this.leaderboardName = leaderboardName;
        this.startOfRace = startOfRace;
        this.eventID = eventID;
    }

    String leaderboardName;
    String eventID;

    public RegattaAndRaceIdentifier getIdentifier() {
        return identifier;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public Date getStartOfRace() {
        return startOfRace;
    }

    public String getEventID() {
        return eventID;
    }

    @Override
    public String toString() {
        return "AnniversaryRaceInfo [identifier=" + identifier + ", leaderboardName=" + leaderboardName
                + ", startOfRace=" + startOfRace + ", eventID=" + eventID + "]";
    }
}
