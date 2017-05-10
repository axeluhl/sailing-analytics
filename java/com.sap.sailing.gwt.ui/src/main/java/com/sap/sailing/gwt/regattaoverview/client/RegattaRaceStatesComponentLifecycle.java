package com.sap.sailing.gwt.regattaoverview.client;

import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RegattaRaceStatesComponentLifecycle implements ComponentLifecycle<RegattaRaceStatesSettings> {
    
    public static final String ID = "rrs";
    
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

    @Override
    public RegattaRaceStatesSettings extractDocumentSettings(RegattaRaceStatesSettings settings) {
        return settings;
    }
}
