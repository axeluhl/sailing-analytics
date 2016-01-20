package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

public class PerspectiveLifecycleAndComponentSettings {
    private final PerspectiveLifecycle<?, ?, ?,?> perspectiveLifecycle;
    private CompositeLifecycleSettings settings;

    public PerspectiveLifecycleAndComponentSettings(PerspectiveLifecycle<?, ?, ?,?> perspectiveLifecycle, 
            CompositeLifecycleSettings settings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.settings = settings;
    }

    public PerspectiveLifecycle<?, ?, ?,?> getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public CompositeLifecycleSettings getSettings() {
        return settings;
    }
    
    public void setSettings(CompositeLifecycleSettings settings) {
        this.settings = settings;
    }
}
