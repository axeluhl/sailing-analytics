package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentConstructionParameters;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class RaceMapLifecycle implements ComponentLifecycle<RaceMap, RaceMapSettings, RaceMapSettingsDialogComponent, RaceMapLifecycle.RaceMapConstructorArgs> {
    private final StringMessages stringMessages;
    
    public static class ConstructionParameters extends ComponentConstructionParameters<RaceMap, RaceMapSettings, RaceMapSettingsDialogComponent, RaceMapLifecycle.RaceMapConstructorArgs> {
        public ConstructionParameters(RaceMapLifecycle componentLifecycle,
                RaceMapConstructorArgs componentConstructorArgs, RaceMapSettings settings) {
            super(componentLifecycle, componentConstructorArgs, settings);
        }
    }

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
        return raceMapContructorArgs.createComponent(settings);
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
