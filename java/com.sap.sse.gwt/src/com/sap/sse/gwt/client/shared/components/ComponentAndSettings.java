package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

/** 
 * A component and it's settings 
 **/
public class ComponentAndSettings<SettingsType extends Settings> implements Serializable {
    private static final long serialVersionUID = -6210586997105550627L;

    private final Component<SettingsType> component;
    private final SettingsType settings;
    
    public ComponentAndSettings(Component<SettingsType> component, SettingsType settings) {
        this.component = component;
        this.settings = settings;
    }

    public Component<SettingsType> getComponent() {
        return component;
    }

    public SettingsType getSettings() {
        return settings;
    }
}