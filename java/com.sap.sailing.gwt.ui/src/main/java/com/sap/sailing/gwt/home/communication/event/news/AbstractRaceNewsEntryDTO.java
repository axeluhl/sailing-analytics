package com.sap.sailing.gwt.home.communication.event.news;

import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public abstract class AbstractRaceNewsEntryDTO extends NewsEntryDTO {
    
    private String leaderboardName;
    private String leaderboardGroupName;
    private UUID leaderboardGroupId;
    private RegattaAndRaceIdentifier regattaAndRaceIdentfier; 
    private String boatClass;

    protected AbstractRaceNewsEntryDTO() {
    }

    @GwtIncompatible
    public AbstractRaceNewsEntryDTO(String leaderboardName, String leaderboardGroupName, UUID leaderboardGroupId, 
            RegattaAndRaceIdentifier regattaAndRaceIdentifier, String raceTitle, String boatClass, Date timestamp) {
        super(raceTitle, timestamp, null);
        this.leaderboardName = leaderboardName;
        this.leaderboardGroupName = leaderboardGroupName;
        this.leaderboardGroupId = leaderboardGroupId;
        this.boatClass = boatClass;
    }
    
    @Override
    public String getBoatClass() {
        return boatClass;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }
    
    public String getLeaderboardGroupName() {
        return leaderboardGroupName;
    }
    
    public UUID getLeaderboardGroupId() {
        return leaderboardGroupId;
    }

    public RegattaAndRaceIdentifier getRegattaAndRaceIdentfier() {
        return regattaAndRaceIdentfier;
    }
}
