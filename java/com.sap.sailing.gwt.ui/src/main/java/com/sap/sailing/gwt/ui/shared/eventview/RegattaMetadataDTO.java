package com.sap.sailing.gwt.ui.shared.eventview;

import java.util.Date;


public class RegattaMetadataDTO extends RegattaReferenceDTO implements HasRegattaMetadata {
    private int raceCount;
    private int competitorsCount;
    private int trackedRacesCount;
    private String boatClass;
    private String boatCategory;
    private Date startDate;
    private Date endDate;
    
    public RegattaMetadataDTO() {
    }
    
    public RegattaMetadataDTO(String id, String name) {
        super(id, name);
    }

    @Override
    public int getRaceCount() {
        return raceCount;
    }

    public void setRaceCount(int raceCount) {
        this.raceCount = raceCount;
    }

    @Override
    public int getCompetitorsCount() {
        return competitorsCount;
    }

    public void setCompetitorsCount(int competitorsCount) {
        this.competitorsCount = competitorsCount;
    }

    @Override
    public int getTrackedRacesCount() {
        return trackedRacesCount;
    }

    public void setTrackedRacesCount(int trackedRacesCount) {
        this.trackedRacesCount = trackedRacesCount;
    }

    @Override
    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }

    @Override
    public String getBoatCategory() {
        return boatCategory;
    }

    public void setBoatCategory(String boatCategory) {
        this.boatCategory = boatCategory;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
