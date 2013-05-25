package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.List;

public class RegattaRaceStatesSettings {
    private List<String> visibleCourseAreas;
    private List<String> visibleRegattas;
    
    private boolean showOnlyRacesOfSameDay;
    private boolean showOnlyCurrentlyRunningRaces;

    public RegattaRaceStatesSettings() {
        this.visibleCourseAreas = new ArrayList<String>();
        this.visibleRegattas = new ArrayList<String>();
        this.showOnlyRacesOfSameDay = false;
        this.showOnlyCurrentlyRunningRaces = false;
    }

    public RegattaRaceStatesSettings(List<String> visibleCourseAreas, List<String> visibleRegattas, 
            boolean showOnlyRacesOfSameDay, boolean showOnlyCurrentlyRunningRaces) {
        this.visibleCourseAreas = visibleCourseAreas;
        this.visibleRegattas = visibleRegattas;
        this.showOnlyRacesOfSameDay = showOnlyRacesOfSameDay;
        this.showOnlyCurrentlyRunningRaces = showOnlyCurrentlyRunningRaces;
    }

    public List<String> getVisibleCourseAreas() {
        return visibleCourseAreas;
    }
    
    public List<String> getVisibleRegattas() {
        return visibleRegattas;
    }

    public boolean isShowOnlyRacesOfSameDay() {
        return showOnlyRacesOfSameDay;
    }
    
    public void setShowOnlyRaceOfSameDay(boolean newValue) {
        showOnlyRacesOfSameDay = newValue;
    }
    
    public boolean isShowOnlyCurrentlyRunningRaces() {
        return showOnlyCurrentlyRunningRaces;
    }
    
    public void setShowOnlyCurrentlyRunningRaces(boolean newValue) {
        showOnlyCurrentlyRunningRaces = newValue;
    }
}
