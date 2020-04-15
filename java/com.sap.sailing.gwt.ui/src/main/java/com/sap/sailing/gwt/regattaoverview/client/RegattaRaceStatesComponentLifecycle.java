package com.sap.sailing.gwt.regattaoverview.client;

import java.util.List;

import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RegattaRaceStatesComponentLifecycle implements ComponentLifecycle<RegattaRaceStatesSettings> {
    
    public static final String ID = "rrs";
    private List<CourseAreaDTO> courseAreaDTOs;
    private List<RaceGroupDTO> raceGroupDTOs;
    
    public RegattaRaceStatesComponentLifecycle(List<CourseAreaDTO> courseAreaDTOs, List<RaceGroupDTO> raceGroupDTOs) {
        this.courseAreaDTOs = courseAreaDTOs;
        this.raceGroupDTOs = raceGroupDTOs;
    }

    @Override
    public RegattaRaceStatesSettingsDialogComponent getSettingsDialogComponent(RegattaRaceStatesSettings settings) {
        // TODO implement if needed
//        return new RegattaRaceStatesSettingsDialogComponent(settings, StringMessages.INSTANCE, contextSettings.getEvent(),
//                settings.getVisibleCourseAreas(),
//                settings.getVisibleRegattas();
        return null;
    }

    @Override
    public RegattaRaceStatesSettings createDefaultSettings() {
        RegattaRaceStatesSettings settings = new RegattaRaceStatesSettings();
        if(courseAreaDTOs != null) {
            settings.setDefaultCourseAreas(courseAreaDTOs);
        }
        if(raceGroupDTOs != null) {
            settings.setDefaultRegattas(raceGroupDTOs);
        }
        return settings;
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.racesOverview();
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public RegattaRaceStatesSettings extractUserSettings(RegattaRaceStatesSettings settings) {
        return settings.createInstanceWithSettings(null, null, settings.isShowOnlyRacesOfSameDay(),
                settings.isShowOnlyCurrentlyRunningRaces());
    }
}
