package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

/**
 * A composite setting for a perspective and it's components.
 * @author Frank
 *
 */
public class PerspectiveCompositeLifecycleSettings<P extends PerspectiveLifecycle<?,S,?>, S extends Settings> extends AbstractSettings {
    private final CompositeLifecycleSettings componentLifeycycleSettings;
    private final PerspectiveLifecycleAndSettings<P,S> perspectiveLifecycleAndSettings;
    
    public PerspectiveCompositeLifecycleSettings(PerspectiveLifecycleAndSettings<P,S> perspectiveLifecycleAndSettings, 
            CompositeLifecycleSettings componentLifeycycleSettings) {
        this.perspectiveLifecycleAndSettings = perspectiveLifecycleAndSettings;
        this.componentLifeycycleSettings = componentLifeycycleSettings;
    }

    public CompositeLifecycleSettings getComponentLifecyclesAndSettings() {
        return componentLifeycycleSettings;
    }

    public PerspectiveLifecycleAndSettings<P, S> getPerspectiveLifecycleAndSettings() {
        return perspectiveLifecycleAndSettings;
    }    

    public boolean hasSettings() {
        return ((perspectiveLifecycleAndSettings != null && perspectiveLifecycleAndSettings.getPerspectiveLifecycle().hasSettings()) ||
                (componentLifeycycleSettings != null && componentLifeycycleSettings.hasSettings()));
    }
}
