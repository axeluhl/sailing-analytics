package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.security.ui.client.UserService;

public class RaceTimePanelLifecycle implements
        ComponentLifecycle<RaceTimePanelSettings> {
    private final StringMessages stringMessages;
    private UserService userService;
    
    public static final String ID = "rt";

    public RaceTimePanelLifecycle(StringMessages stringMessages, UserService userService) {
        this.stringMessages = stringMessages;
        this.userService = userService;
    }

    @Override
    public RaceTimePanelSettingsDialogComponent getSettingsDialogComponent(RaceTimePanelSettings settings) {
        return new RaceTimePanelSettingsDialogComponent(settings, stringMessages, userService);
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
}
