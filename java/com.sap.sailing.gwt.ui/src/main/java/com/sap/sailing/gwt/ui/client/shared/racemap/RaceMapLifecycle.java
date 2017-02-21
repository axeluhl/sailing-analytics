package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceMapLifecycle implements ComponentLifecycle<RaceMapSettings> {
    public static final String ID = "rml";

    private final StringMessages stringMessages;
    
    public RaceMapLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }
    
    @Override
    public RaceMapSettingsDialogComponent getSettingsDialogComponent(RaceMapSettings settings) {
        return new RaceMapSettingsDialogComponent(settings, stringMessages,
                /* isSimulationEnabled: enable simulation because we don't know the boat class
                 * here yet and therefore cannot reasonably judge whether polar data is
                 * available; if in doubt, rather enable selecting it */ true);
    }

    @Override
    public RaceMapSettings createDefaultSettings() {
        return new RaceMapSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.map();
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
    public RaceMapSettings extractGlobalSettings(RaceMapSettings settings) {
        return createDefaultSettings();
    }

    @Override
    public RaceMapSettings extractContextSettings(RaceMapSettings settings) {
        return settings;
    }
}
