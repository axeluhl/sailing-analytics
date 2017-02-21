package com.sap.sailing.gwt.regattaoverview.client;

import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RegattaRaceStatesComponentLifecycle implements ComponentLifecycle<RegattaRaceStatesSettings> {
    
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
        return new RegattaRaceStatesSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.racesOverview();
    }

    @Override
    public String getComponentId() {
        return "rrs";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public RegattaRaceStatesSettings extractGlobalSettings(RegattaRaceStatesSettings settings) {
        return settings.createInstanceWithSettings(null, null, settings.isShowOnlyRacesOfSameDay(),
                settings.isShowOnlyCurrentlyRunningRaces());
    }

    @Override
    public RegattaRaceStatesSettings extractContextSettings(RegattaRaceStatesSettings settings) {
        final RegattaRaceStatesSettings defaultSettings = new RegattaRaceStatesSettings();
        return settings.createInstanceWithSettings(settings.getVisibleCourseAreas(), settings.getVisibleRegattas(),
                defaultSettings.isShowOnlyRacesOfSameDay(), defaultSettings.isShowOnlyCurrentlyRunningRaces());
    }
}
