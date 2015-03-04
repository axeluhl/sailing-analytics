package com.sap.sailing.gwt.ui.shared.eventview;

import java.util.Date;

public interface HasRegattaMetadata {
    
    String getDisplayName();

    int getRaceCount();

    int getCompetitorsCount();

    int getTrackedRacesCount();

    String getBoatClass();
    
    String getBoatCategory();

    Date getStartDate();

    Date getEndDate();
}
