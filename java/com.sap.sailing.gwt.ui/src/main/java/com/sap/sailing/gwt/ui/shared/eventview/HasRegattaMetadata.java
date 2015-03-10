package com.sap.sailing.gwt.ui.shared.eventview;

import java.util.Date;

public interface HasRegattaMetadata {
    
    public enum RegattaState {
        UPCOMING, RUNNING, FINISHED, UNKNOWN
    }
    
    String getDisplayName();

    int getRaceCount();

    int getCompetitorsCount();

    int getTrackedRacesCount();

    String getBoatClass();
    
    String getBoatCategory();

    Date getStartDate();

    Date getEndDate();
    
    RegattaState getState();
}
