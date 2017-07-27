package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class AnniversaryRaceInfo {
    public AnniversaryRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, Date startOfRace,
            String eventID,String remoteUrl) {
        super();
        this.identifier = identifier;
        this.leaderboardName = leaderboardName;
        this.startOfRace = startOfRace;
        this.eventID = eventID;
        this.remoteUrl = remoteUrl;
    }

    RegattaAndRaceIdentifier identifier;
    String leaderboardName;
    Date startOfRace;
    String eventID;
    String remoteUrl;

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
}
