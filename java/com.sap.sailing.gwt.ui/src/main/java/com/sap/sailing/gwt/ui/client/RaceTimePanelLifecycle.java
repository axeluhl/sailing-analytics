package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceTimePanelLifecycle implements
        ComponentLifecycle<RaceTimePanelSettings> {
    private final StringMessages stringMessages;

    public RaceTimePanelLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public RaceTimePanelSettingsDialogComponent getSettingsDialogComponent(RaceTimePanelSettings settings) {
        return new RaceTimePanelSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public RaceTimePanelSettings createDefaultSettings() {
        return new RaceTimePanelSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return "TimePanel";
    }

    @Override
    public String getComponentId() {
        return "rtpl";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public RaceTimePanelSettings extractGlobalSettings(RaceTimePanelSettings settings) {
        return settings;
    }

    @Override
    public RaceTimePanelSettings extractContextSettings(RaceTimePanelSettings settings) {
        return createDefaultSettings();
    }
}
