package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceTimePanelLifecycle implements
        ComponentLifecycle<RaceTimePanelSettings> {
    private final StringMessages stringMessages;
    
    public static final String ID = "rt";

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
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public RaceTimePanelSettings extractUserSettings(RaceTimePanelSettings settings) {
        return settings;
    }

    @Override
    public RaceTimePanelSettings extractDocumentSettings(RaceTimePanelSettings settings) {
        return settings;
    }
}
