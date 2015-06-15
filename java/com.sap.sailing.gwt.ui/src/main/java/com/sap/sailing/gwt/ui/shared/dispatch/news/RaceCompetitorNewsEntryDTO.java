package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

public class RaceCompetitorNewsEntryDTO extends AbstractRaceNewsEntryDTO {
    
    public enum Type {
        WINNER
    }
    
    private String competitorName;
    private Type type;

    @SuppressWarnings("unused")
    private RaceCompetitorNewsEntryDTO() {
    }
    
    public RaceCompetitorNewsEntryDTO(String regattaName, String trackedRaceName, String raceTitle, String boatClass, Date timestamp, String competitorName, Type type) {
        super(regattaName, trackedRaceName, raceTitle, boatClass, timestamp);
        this.competitorName = competitorName;
        this.type = type;
    }

    @Override
    public String getMessage() {
        // TODO I18n...
        switch (type) {
        case WINNER:
            return competitorName + " won the race";
        }
        return "";
    }
}
