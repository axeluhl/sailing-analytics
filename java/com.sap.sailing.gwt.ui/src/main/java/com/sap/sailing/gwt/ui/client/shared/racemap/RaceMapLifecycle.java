package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.io.Serializable;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceMapLifecycle implements ComponentLifecycle<RaceMapSettings, RaceMapSettingsDialogComponent> {
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
    public RaceMapSettings cloneSettings(RaceMapSettings settings) {
        return new RaceMapSettings(settings);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.map();
    }

    @Override
    public Serializable getComponentId() {
        return getLocalizedShortName();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
