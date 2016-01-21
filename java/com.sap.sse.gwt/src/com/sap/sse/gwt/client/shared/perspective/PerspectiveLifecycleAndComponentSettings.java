package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

public class PerspectiveLifecycleAndComponentSettings {
    private final PerspectiveLifecycle<?, ?, ?,?> perspectiveLifecycle;
    private CompositeLifecycleSettings componentSettings;

    public PerspectiveLifecycleAndComponentSettings(PerspectiveLifecycle<?, ?, ?,?> perspectiveLifecycle, 
            CompositeLifecycleSettings settings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.componentSettings = settings;
    }

    public PerspectiveLifecycle<?, ?, ?,?> getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public CompositeLifecycleSettings getComponentSettings() {
        return componentSettings;
    }
    
    public void setComponentSettings(CompositeLifecycleSettings componentSettings) {
        this.componentSettings = componentSettings;
    }
}
