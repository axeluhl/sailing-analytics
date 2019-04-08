package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class ManeuverTableLifecycle implements ComponentLifecycle<ManeuverTableSettings> {
    private final StringMessages stringMessages;
    
    public static final String ID = "mt";

    public ManeuverTableLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public ManeuverTableSettingsDialogComponent getSettingsDialogComponent(ManeuverTableSettings settings) {
        return new ManeuverTableSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public ManeuverTableSettings createDefaultSettings() {
        return new ManeuverTableSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.maneuverTable();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public String getComponentId() {
        return ID;
    }
}
