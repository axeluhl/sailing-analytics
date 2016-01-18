package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public class ComponentLifecycleAndSettings<SettingsType extends Settings> {
    private final ComponentLifecycle<?, SettingsType, ?,?> componentLifecycle;
    private SettingsType settings;

    public ComponentLifecycleAndSettings(ComponentLifecycle<?, SettingsType, ?,?> componentLifecycle,
            SettingsType settings) {
        super();
        this.componentLifecycle = componentLifecycle;
        this.settings = settings;
    }

    public ComponentLifecycle<?, SettingsType, ?,?> getComponentLifecycle() {
        return componentLifecycle;
    }
    
    public SettingsType getSettings() {
        return settings;
    }
    
    public void setSettings(SettingsType settings) {
        this.settings = settings;
    }
}
