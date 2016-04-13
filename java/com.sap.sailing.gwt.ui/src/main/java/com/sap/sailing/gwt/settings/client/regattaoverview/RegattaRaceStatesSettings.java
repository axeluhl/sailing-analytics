package com.sap.sailing.gwt.settings.client.regattaoverview;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.BooleanSetting;
import com.sap.sse.common.settings.StringSetSetting;
import com.sap.sse.common.settings.UUIDSetSetting;

public final class RegattaRaceStatesSettings extends AbstractSettings {
    private UUIDSetSetting visibleCourseAreas;
    private StringSetSetting visibleRegattas;
    private BooleanSetting showOnlyRacesOfSameDay;
    private BooleanSetting showOnlyCurrentlyRunningRaces;

    public RegattaRaceStatesSettings() {
    }
    
    public RegattaRaceStatesSettings(Iterable<CourseAreaDTO> defaultCourseAreas, Iterable<RaceGroupDTO> defaultRaceGroups) {
        setDefaultCourseAreas(defaultCourseAreas);
        setDefaultRegattas(defaultRaceGroups);
    }

    public RegattaRaceStatesSettings(Iterable<CourseAreaDTO> defaultCourseAreas, Iterable<UUID> visibleCourseAreas,
            Iterable<RaceGroupDTO> defaultRaceGroups, Iterable<String> visibleRegattas, boolean showOnlyRacesOfSameDay,
            boolean showOnlyCurrentlyRunningRaces) {
        this(defaultCourseAreas, defaultRaceGroups);
        this.visibleCourseAreas.setValues(visibleCourseAreas);
        this.visibleRegattas.setValues(visibleRegattas);
        this.showOnlyRacesOfSameDay.setValue(showOnlyRacesOfSameDay);
        this.showOnlyCurrentlyRunningRaces.setValue(showOnlyCurrentlyRunningRaces);
    }
    
    @Override
    protected void addChildSettings() {
        visibleCourseAreas = new UUIDSetSetting("visibleCourseAreas", this, true);
        visibleRegattas = new StringSetSetting("visibleRegattas", this, true);
        showOnlyRacesOfSameDay = new BooleanSetting("showOnlyRacesOfSameDay", this, false);
        showOnlyCurrentlyRunningRaces = new BooleanSetting("showOnlyCurrentlyRunningRaces", this, true);
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
    
    public void setDefaultCourseAreas(Iterable<CourseAreaDTO> defaultCourseAreas) {
        if(defaultCourseAreas == null) {
            defaultCourseAreas = Collections.emptySet();
        }
        Set<UUID> courseAreaIds = new HashSet<>();
        for (CourseAreaDTO courseArea : defaultCourseAreas) {
            courseAreaIds.add(courseArea.id);
        }
        visibleCourseAreas.setDefaultValues(courseAreaIds);
    }
    
    public void setDefaultRegattas(Iterable<RaceGroupDTO> defaultRaceGroups) {
        if(defaultRaceGroups == null) {
            defaultRaceGroups = Collections.emptySet();
        }
        Set<String> regattaIds = new HashSet<>();
        for (RaceGroupDTO raceGroup : defaultRaceGroups) {
            regattaIds.add(raceGroup.getName());
        }
        visibleRegattas.setDefaultValues(regattaIds);
    }

}
