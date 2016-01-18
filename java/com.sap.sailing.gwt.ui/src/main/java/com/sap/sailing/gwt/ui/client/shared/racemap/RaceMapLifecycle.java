package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceMapLifecycle implements ComponentLifecycle<RaceMap, RaceMapSettings, RaceMapSettingsDialogComponent, RaceMapLifecycle.RaceMapConstructorArgs> {
    private final StringMessages stringMessages;
    
    public RaceMapLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }
    
    @Override
    public RaceMapSettingsDialogComponent getSettingsDialogComponent(RaceMapSettings settings) {
        return new RaceMapSettingsDialogComponent(settings, stringMessages, false);
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
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public RaceMap createComponent(RaceMapConstructorArgs raceMapContructorArgs, RaceMapSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    public class RaceMapConstructorArgs implements ComponentConstructorArgs<RaceMap, RaceMapSettings> {
        public RaceMapConstructorArgs() {
        }
        
        @Override
        public RaceMap createComponent(RaceMapSettings newSettings) {
            return null;
        }
    }
    
}
