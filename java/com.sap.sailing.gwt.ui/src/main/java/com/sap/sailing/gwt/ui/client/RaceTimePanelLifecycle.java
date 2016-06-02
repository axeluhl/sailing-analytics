package com.sap.sailing.gwt.ui.client;

import java.io.Serializable;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceTimePanelLifecycle implements
        ComponentLifecycle<RaceTimePanelSettings, RaceTimePanelSettingsDialogComponent> {
    private final StringMessages stringMessages;

    public RaceTimePanelLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public RaceTimePanelSettingsDialogComponent getSettingsDialogComponent(RaceTimePanelSettings settings) {
        return new RaceTimePanelSettingsDialogComponent(cloneSettings(settings), stringMessages);
    }

    @Override
    public RaceTimePanelSettings createDefaultSettings() {
        return new RaceTimePanelSettings(1000);
    }

    @Override
    public RaceTimePanelSettings cloneSettings(RaceTimePanelSettings settings) {
        return new RaceTimePanelSettings(settings.getRefreshInterval());
    }

    @Override
    public String getLocalizedShortName() {
        return "TimePanel";
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
