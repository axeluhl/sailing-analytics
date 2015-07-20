package com.sap.sailing.gwt.ui.shared.eventview;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.general.LabelType;

public interface HasRegattaMetadata {
    
    public enum RegattaState {
        UPCOMING(LabelType.UPCOMING), PROGRESS(LabelType.PROGRESS), RUNNING(LabelType.LIVE), FINISHED(LabelType.FINISHED), UNKNOWN(LabelType.NONE);
        
        private final LabelType stateMarker;

        private RegattaState(LabelType stateMarker) {
            this.stateMarker = stateMarker;
        }
        
        public LabelType getStateMarker() {
            return stateMarker;
        }
    }
    
    String getDisplayName();

    int getRaceCount();

    int getCompetitorsCount();

    String getBoatClass();
    
    String getBoatCategory();

    Date getStartDate();

    Date getEndDate();
    
    RegattaState getState();

    String getDefaultCourseAreaName();
}
