package com.sap.sailing.gwt.regattaoverview.client;

import java.util.UUID;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.BooleanSetting;
import com.sap.sse.common.settings.StringSetSetting;
import com.sap.sse.common.settings.UUIDSetSetting;

public final class RegattaRaceStatesSettings extends AbstractSettings {
    private final UUIDSetSetting visibleCourseAreas = new UUIDSetSetting("visibleCourseAreas", this);
    private final StringSetSetting visibleRegattas = new StringSetSetting("visibleRegattas", this);
    private final BooleanSetting showOnlyRacesOfSameDay = new BooleanSetting("showOnlyRacesOfSameDay", this, false);
    private final BooleanSetting showOnlyCurrentlyRunningRaces = new BooleanSetting("showOnlyCurrentlyRunningRaces",
            this, true);

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

    public void setVisibleCourseAreas(Iterable<UUID> visibleCourseAreas) {
        this.visibleCourseAreas.setValues(visibleCourseAreas);
    }
    
    public UUIDSetSetting getVisibleCourseAreaSettings() {
        return visibleCourseAreas;
    }

    public Iterable<String> getVisibleRegattas() {
        return visibleRegattas.getValues();
    }

    public void setVisibleRegattas(Iterable<String> visibleRegattas) {
        this.visibleRegattas.setValues(visibleRegattas);
    }

    public StringSetSetting getVisibleRegattaSettings() {
        return visibleRegattas;
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
