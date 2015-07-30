package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public abstract class AbstractRaceNewsEntryDTO extends NewsEntryDTO {
    
    private String leaderboardName;
    private RegattaAndRaceIdentifier regattaAndRaceIdentfier; 
    private String boatClass;

    protected AbstractRaceNewsEntryDTO() {
    }

    @GwtIncompatible
    public AbstractRaceNewsEntryDTO(String leaderboardName, RegattaAndRaceIdentifier regattaAndRaceIdentifier, String raceTitle, String boatClass, Date timestamp) {
        super(raceTitle, timestamp, null);
        this.leaderboardName = leaderboardName;
        this.boatClass = boatClass;
    }
    
    @Override
    public String getBoatClass() {
        return boatClass;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public RegattaAndRaceIdentifier getRegattaAndRaceIdentfier() {
        return regattaAndRaceIdentfier;
    }
}
