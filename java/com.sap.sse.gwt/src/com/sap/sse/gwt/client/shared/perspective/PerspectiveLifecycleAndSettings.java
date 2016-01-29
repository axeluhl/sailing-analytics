package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public class PerspectiveLifecycleAndSettings<PerspectiveLifecycleType extends PerspectiveLifecycle<?, SettingsType, ?,?>, SettingsType extends Settings> {
    private final PerspectiveLifecycleType perspectiveLifecycle;
    private SettingsType settings;

    public PerspectiveLifecycleAndSettings(PerspectiveLifecycleType perspectiveLifecycle, SettingsType settings) {
        super();
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.settings = settings;
    }

    public PerspectiveLifecycleType getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public SettingsType getSettings() {
        return settings;
    }
    
    public void setSettings(SettingsType settings) {
        this.settings = settings;
    }
}
