package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public class ComponentLifecycleAndSettings<ComponentLifecycleType extends ComponentLifecycle<SettingsType, ?>, SettingsType extends Settings> {
    private final ComponentLifecycleType componentLifecycle;
    private final SettingsType settings;

    public ComponentLifecycleAndSettings(ComponentLifecycleType componentLifecycle, SettingsType settings) {
        super();
        this.componentLifecycle = componentLifecycle;
        this.settings = settings;
    }

    public ComponentLifecycleType getComponentLifecycle() {
        return componentLifecycle;
    }
    
    public SettingsType getSettings() {
        return settings;
    }
}
