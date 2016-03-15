package com.sap.sailing.gwt.regattaoverview.client;

import java.util.UUID;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.BooleanSetting;
import com.sap.sse.common.settings.StringListSetting;
import com.sap.sse.common.settings.UUIDListSetting;

public class RegattaRaceStatesSettings extends AbstractSettings {
    private UUIDListSetting visibleCourseAreas = new UUIDListSetting("visibleCourseAreas", this);
    private StringListSetting visibleRegattas = new StringListSetting("visibleRegattas", this);
    private BooleanSetting showOnlyRacesOfSameDay = new BooleanSetting("showOnlyRacesOfSameDay", this, false);
    private BooleanSetting showOnlyCurrentlyRunningRaces = new BooleanSetting("showOnlyCurrentlyRunningRaces", this,
            true);

    public RegattaRaceStatesSettings() {
    }

    public RegattaRaceStatesSettings(Iterable<UUID> visibleCourseAreas, Iterable<String> visibleRegattas,
            boolean showOnlyRacesOfSameDay, boolean showOnlyCurrentlyRunningRaces) {
        this.visibleCourseAreas.setValues(visibleCourseAreas);
        this.visibleRegattas.setValues(visibleRegattas);
        this.showOnlyRacesOfSameDay.setValue(showOnlyRacesOfSameDay);
        this.showOnlyCurrentlyRunningRaces.setValue(showOnlyCurrentlyRunningRaces);
    }

    public Iterable<UUID> getVisibleCourseAreas() {
        return visibleCourseAreas.getValues();
    }
    
    public Iterable<String> getVisibleRegattas() {
        return visibleRegattas.getValues();
    }

    public void setVisibleCourseAreas(Iterable<UUID> visibleCourseAreas) {
        this.visibleCourseAreas.setValues(visibleCourseAreas);
    }

    public void addVisibleCourseArea(UUID value) {
        visibleCourseAreas.addValue(value);
    }

    public void clearVisibleCourseAreas() {
        visibleCourseAreas.clear();
    }

    public void setVisibleRegattas(Iterable<String> visibleRegattas) {
        this.visibleRegattas.setValues(visibleRegattas);
    }

    public void addVisibleRegatta(String value) {
        visibleRegattas.addValue(value);
    }

    public void clearVisibleRegattas() {
        visibleRegattas.clear();
    }

    public boolean isShowOnlyRacesOfSameDay() {
        return showOnlyRacesOfSameDay.getValue();
    }
    
    public void setShowOnlyRaceOfSameDay(boolean newValue) {
        showOnlyRacesOfSameDay.setValue(newValue);
    }
    
    public boolean isShowOnlyCurrentlyRunningRaces() {
        return showOnlyCurrentlyRunningRaces.getValue();
    }
    
    public void setShowOnlyCurrentlyRunningRaces(boolean newValue) {
        showOnlyCurrentlyRunningRaces.setValue(newValue);
    }

}
