package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

/** 
 * A perspective and it's perspective specific settings 
 **/
public class PerspectiveAndSettings<SettingsType extends Settings> implements Serializable {
    private static final long serialVersionUID = -6750964868964305325L;

    private final Perspective<SettingsType> perspective;
    private final SettingsType settings;
    
    public PerspectiveAndSettings(Perspective<SettingsType> perspective, SettingsType settings) {
        this.perspective = perspective;
        this.settings = settings;
    }

    public Perspective<SettingsType> getPerspective() {
        return perspective;
    }

    public SettingsType getSettings() {
        return settings;
    }
}