package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class EventStatisticsDTO implements DTO {

    private Integer regattasFoughtCount;
    private Integer competitorsCount;
    private Integer racesRunCount;
    private Integer trackedRacesCount;
    private Integer rawGPSFixes;
    private Integer sumSailedMiles;
    private Integer fastestSailorSpeed;
    private String fastestSailorInfo;
    private String strongestWind;
    
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

    public Integer getRawGPSFixes() {
        return rawGPSFixes;
    }

    public void setRawGPSFixes(Integer rawGPSFixes) {
        this.rawGPSFixes = rawGPSFixes;
    }

    public Integer getSumSailedMiles() {
        return sumSailedMiles;
    }

    public void setSumSailedMiles(Integer sumSailedMiles) {
        this.sumSailedMiles = sumSailedMiles;
    }

    public Integer getFastestSailorSpeed() {
        return fastestSailorSpeed;
    }

    public void setFastestSailorSpeed(Integer fastestSailorSpeed) {
        this.fastestSailorSpeed = fastestSailorSpeed;
    }

    public String getFastestSailorInfo() {
        return fastestSailorInfo;
    }

    public void setFastestSailorInfo(String fastestSailorInfo) {
        this.fastestSailorInfo = fastestSailorInfo;
    }

    public String getStrongestWind() {
        return strongestWind;
    }

    public void setStrongestWind(String strongestWind) {
        this.strongestWind = strongestWind;
    }
}
