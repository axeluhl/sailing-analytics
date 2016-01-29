package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

public class PerspectiveLifecycleAndComponentSettings<PerspectiveLifecycleType extends PerspectiveLifecycle<?, ?, ?,?>> {
    private final PerspectiveLifecycleType perspectiveLifecycle;
    private CompositeLifecycleSettings componentSettings;

    public PerspectiveLifecycleAndComponentSettings(PerspectiveLifecycleType perspectiveLifecycle, 
            CompositeLifecycleSettings settings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.componentSettings = settings;
    }

    public PerspectiveLifecycleType getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }
    
    public CompositeLifecycleSettings getComponentSettings() {
        return componentSettings;
    }
    
    public void setComponentSettings(CompositeLifecycleSettings componentSettings) {
        this.componentSettings = componentSettings;
    }
}
