package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

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
    
    public RaceCompetitorNewsEntryDTO(String regattaName, String trackedRaceName, String raceTitle, String boatClass, Date timestamp, String competitorName, Type type, Date currentTimestamp) {
        super(regattaName, trackedRaceName, raceTitle, boatClass, timestamp, currentTimestamp);
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
