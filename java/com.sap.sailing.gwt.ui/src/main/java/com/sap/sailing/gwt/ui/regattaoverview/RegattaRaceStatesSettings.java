package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.settings.Settings;

public class RegattaRaceStatesSettings implements Settings {
    private List<UUID> visibleCourseAreas;
    private List<String> visibleRegattas;
    
    private boolean showOnlyRacesOfSameDay;
    private boolean showOnlyCurrentlyRunningRaces;

    public RegattaRaceStatesSettings() {
        this.visibleCourseAreas = new ArrayList<UUID>();
        this.visibleRegattas = new ArrayList<String>();
        this.showOnlyRacesOfSameDay = false;
        this.showOnlyCurrentlyRunningRaces = true;
    }

    public RegattaRaceStatesSettings(List<UUID> visibleCourseAreas, List<String> visibleRegattas, 
            boolean showOnlyRacesOfSameDay, boolean showOnlyCurrentlyRunningRaces) {
        this.visibleCourseAreas = visibleCourseAreas;
        this.visibleRegattas = visibleRegattas;
        this.showOnlyRacesOfSameDay = showOnlyRacesOfSameDay;
        this.showOnlyCurrentlyRunningRaces = showOnlyCurrentlyRunningRaces;
    }

    public List<UUID> getVisibleCourseAreas() {
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
