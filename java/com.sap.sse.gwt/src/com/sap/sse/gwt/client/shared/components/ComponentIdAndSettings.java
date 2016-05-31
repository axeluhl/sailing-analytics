package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

/** 
 * A componentId and it's settings 
 **/
public class ComponentIdAndSettings<SettingsType extends Settings> implements Serializable {
    private static final long serialVersionUID = -6210586997105550627L;

    private final Serializable componentId;
    private final SettingsType settings;
    
    public ComponentIdAndSettings(Serializable componentId, SettingsType settings) {
        this.componentId = componentId;
        this.settings = settings;
    }

    public Serializable getComponentId() {
        return componentId;
    }

    public SettingsType getSettings() {
        return settings;
    }
}