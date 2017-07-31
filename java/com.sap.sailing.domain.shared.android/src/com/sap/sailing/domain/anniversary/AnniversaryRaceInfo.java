package com.sap.sailing.domain.anniversary;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class AnniversaryRaceInfo {
    public AnniversaryRaceInfo(RegattaAndRaceIdentifier identifier, String leaderboardName, Date startOfRace,
            String eventID, String remoteUrl) {
        super();
        if(identifier == null || leaderboardName == null || startOfRace == null || eventID==null||remoteUrl==null){
            throw new IllegalStateException("Anniversary Data is not allowed to contain any null values!");
        }
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
    
    public String getRemoteUrl() {
        return remoteUrl;
    }

    @Override
    public String toString() {
        return "AnniversaryRaceInfo [identifier=" + identifier + ", leaderboardName=" + leaderboardName
                + ", startOfRace=" + startOfRace + ", eventID=" + eventID + ", remoteUrl=" + remoteUrl + "]";
    }
}
