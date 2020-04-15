package com.sap.sailing.gwt.home.communication.eventview;

import java.util.Date;

import com.sap.sailing.gwt.home.communication.event.EventSeriesReferenceDTO;
import com.sap.sailing.gwt.home.communication.event.LabelType;

public interface HasRegattaMetadata {
    
    public enum RegattaState {
        UPCOMING(LabelType.UPCOMING), PROGRESS(LabelType.PROGRESS), RUNNING(LabelType.LIVE), FINISHED(LabelType.FINISHED);
        
        private final LabelType stateMarker;

        private RegattaState() {
            // For GWT serialization only
            stateMarker = null;
        }
        
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
    
    Iterable<String> getLeaderboardGroupNames();

    Date getStartDate();

    Date getEndDate();
    
    RegattaState getState();

    String getDefaultCourseAreaName();

    boolean isFlexibleLeaderboard();

    String getDefaultCourseAreaId();
    
    EventSeriesReferenceDTO getSeriesReference();
}
