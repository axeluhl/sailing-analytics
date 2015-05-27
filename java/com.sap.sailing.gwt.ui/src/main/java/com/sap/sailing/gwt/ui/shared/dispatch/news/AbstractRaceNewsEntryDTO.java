package com.sap.sailing.gwt.ui.shared.dispatch.news;

import java.util.Date;

public abstract class AbstractRaceNewsEntryDTO extends NewsEntryDTO {
    
    private String regattaName;
    private String trackedRaceName;
    private String raceTitle;

    @SuppressWarnings("unused")
    private AbstractRaceNewsEntryDTO() {
    }

    public AbstractRaceNewsEntryDTO(String regattaName, String trackedRaceName, String raceTitle, Date timestamp) {
        super(timestamp);
        this.setRegattaName(regattaName);
        this.setTrackedRaceName(trackedRaceName);
        this.raceTitle = raceTitle;
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
