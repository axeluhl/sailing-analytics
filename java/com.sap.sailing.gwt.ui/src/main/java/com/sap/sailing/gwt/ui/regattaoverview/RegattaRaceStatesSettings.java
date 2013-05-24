package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class RegattaRaceStatesSettings {
    private List<RaceLogRaceStatus> visibleRaceStates;
    private List<String> visibleCourseAreas;
    private List<String> visibleRegattas;
    
    private boolean showOnlyRacesOfSameDay;

    public RegattaRaceStatesSettings() {
        this.visibleRaceStates = new ArrayList<RaceLogRaceStatus>();
        this.visibleCourseAreas = new ArrayList<String>();
        this.visibleRegattas = new ArrayList<String>();
        this.showOnlyRacesOfSameDay = false;
    }

    public RegattaRaceStatesSettings(List<RaceLogRaceStatus> visibleRaceStates, List<String> visibleCourseAreas,
            List<String> visibleRegattas, boolean showOnlyRacesOfSameDay) {
        this.visibleRaceStates = visibleRaceStates;
        this.visibleCourseAreas = visibleCourseAreas;
        this.visibleRegattas = visibleRegattas;
        this.showOnlyRacesOfSameDay = showOnlyRacesOfSameDay;
    }

    public List<RaceLogRaceStatus> getVisibleRaceStates() {
        return visibleRaceStates;
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
}
