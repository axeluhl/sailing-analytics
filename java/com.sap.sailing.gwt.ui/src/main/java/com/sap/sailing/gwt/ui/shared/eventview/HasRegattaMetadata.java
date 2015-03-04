package com.sap.sailing.gwt.ui.shared.eventview;

public interface HasRegattaMetadata {
    
    String getDisplayName();

    int getRaceCount();

    int getCompetitorsCount();

    int getTrackedRacesCount();

    String getBoatClass();
    
    String getBoatCategory();
}
