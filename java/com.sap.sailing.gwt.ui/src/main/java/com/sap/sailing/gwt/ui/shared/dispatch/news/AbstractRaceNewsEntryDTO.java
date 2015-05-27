package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

public abstract class AbstractRaceNewsEntryDTO extends NewsEntryDTO {
    
    private String regattaName;
    private String trackedRaceName;
    private String raceTitle;
    private String boatClass;

    protected AbstractRaceNewsEntryDTO() {
    }

    public AbstractRaceNewsEntryDTO(String regattaName, String trackedRaceName, String raceTitle, String boatClass, Date timestamp) {
        super(timestamp);
        this.regattaName = regattaName;
        this.trackedRaceName = trackedRaceName;
        this.boatClass = boatClass;
        this.raceTitle = raceTitle;
    }
    
    @Override
    public String getBoatClass() {
        return boatClass;
    }

    @Override
    public String getTitle() {
        return raceTitle;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
    }

    public String getTrackedRaceName() {
        return trackedRaceName;
    }

    public void setTrackedRaceName(String trackedRaceName) {
        this.trackedRaceName = trackedRaceName;
    }
}
