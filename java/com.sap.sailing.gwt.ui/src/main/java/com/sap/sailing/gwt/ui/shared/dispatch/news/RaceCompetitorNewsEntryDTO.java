package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class RaceCompetitorNewsEntryDTO extends AbstractRaceNewsEntryDTO {
    
    public enum Type {
        WINNER, CRASH
    }
    
    private CompetitorDTO competitor;
    private Type type;

    @SuppressWarnings("unused")
    private RaceCompetitorNewsEntryDTO() {
    }
    
    public RaceCompetitorNewsEntryDTO(String regattaName, String trackedRaceName, String raceTitle, String boatClass, Date timestamp, CompetitorDTO competitor, Type type) {
        super(regattaName, trackedRaceName, raceTitle, boatClass, timestamp);
        this.competitor = competitor;
        this.type = type;
    }

    @Override
    public String getMessage() {
        // TODO I18n...
        switch (type) {
        case WINNER:
            return competitor.getName() + " won the race";
        case CRASH:
            return competitor.getName() + " crashed";
        }
        return competitor.getName();
    }
}
