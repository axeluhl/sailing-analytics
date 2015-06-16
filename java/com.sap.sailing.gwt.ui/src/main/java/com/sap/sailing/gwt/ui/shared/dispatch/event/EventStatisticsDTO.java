package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class EventStatisticsDTO implements DTO {

    private Integer regattasFoughtCount;
    private Integer competitorsCount;
    private Integer racesRunCount;
    private Integer trackedRacesCount;
    
    @SuppressWarnings("unused")
    private EventStatisticsDTO() {
    }
    
    public EventStatisticsDTO(Integer regattasFoughtCount, Integer competitorsCount, Integer racesRunCount,
            Integer trackedRacesCount) {
        super();
        this.regattasFoughtCount = regattasFoughtCount;
        this.competitorsCount = competitorsCount;
        this.racesRunCount = racesRunCount;
        this.trackedRacesCount = trackedRacesCount;
    }



    public Integer getRegattasFoughtCount() {
        return regattasFoughtCount;
    }

    public void setRegattasFoughtCount(Integer regattasFoughtCount) {
        this.regattasFoughtCount = regattasFoughtCount;
    }

    public Integer getCompetitorsCount() {
        return competitorsCount;
    }

    public void setCompetitorsCount(Integer competitorsCount) {
        this.competitorsCount = competitorsCount;
    }

    public Integer getRacesRunCount() {
        return racesRunCount;
    }

    public void setRacesRunCount(Integer racesRunCount) {
        this.racesRunCount = racesRunCount;
    }

    public Integer getTrackedRacesCount() {
        return trackedRacesCount;
    }

    public void setTrackedRacesCount(Integer trackedRacesCount) {
        this.trackedRacesCount = trackedRacesCount;
    }

}
