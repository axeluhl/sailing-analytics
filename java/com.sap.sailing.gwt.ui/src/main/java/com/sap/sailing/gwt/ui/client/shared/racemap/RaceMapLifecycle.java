package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceMapLifecycle implements ComponentLifecycle<RaceMap, RaceMapSettings, RaceMapSettingsDialogComponent> {
    private final StringMessages stringMessages;
    private RaceMap component;
    
    public RaceMapLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.component = null;
    }
    
    @Override
    public RaceMapSettingsDialogComponent getSettingsDialogComponent(RaceMapSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceMap getComponent() {
        return component;
    }

    @Override
    public RaceMapSettings createDefaultSettings() {
        return new RaceMapSettings();
    }

    @Override
    public RaceMapSettings cloneSettings(RaceMapSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.map();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

}
