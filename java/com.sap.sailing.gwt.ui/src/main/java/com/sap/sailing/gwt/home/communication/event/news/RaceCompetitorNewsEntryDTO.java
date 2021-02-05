package com.sap.sailing.gwt.home.communication.event.news;

import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RaceCompetitorNewsEntryDTO extends AbstractRaceNewsEntryDTO {
    
    public enum Type {
        WINNER
    }
    
    private String competitorName;
    private Type type;

    @SuppressWarnings("unused")
    private RaceCompetitorNewsEntryDTO() {
    }
    
    @GwtIncompatible
    public RaceCompetitorNewsEntryDTO(String leaderboardName, String leaderboardGroupName, UUID leaderboardGroupId, 
            RegattaAndRaceIdentifier regattaAndRaceIdentifier, String raceTitle, String boatClass, Date timestamp,
            String competitorName, Type type) {
        super(leaderboardName, leaderboardGroupName, leaderboardGroupId, regattaAndRaceIdentifier, raceTitle, boatClass,
                timestamp);
        this.competitorName = competitorName;
        this.type = type;
    }

    @Override
    public String getMessage() {
        switch (type) {
        case WINNER:
            return StringMessages.INSTANCE.competitorWonRace(competitorName);
        }
        return "";
    }
}
