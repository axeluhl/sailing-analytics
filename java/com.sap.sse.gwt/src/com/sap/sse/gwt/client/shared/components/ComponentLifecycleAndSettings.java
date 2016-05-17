package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

/**
 * A utility class to combine a {@link ComponentLifecycle} and the settings of the corresponding {@link Component} 
 * @author Frank
 *
 * @param <ComponentLifecycleType>
 * @param <SettingsType>
 */
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
