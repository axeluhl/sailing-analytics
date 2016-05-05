package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

/**
 * A composite setting for the perspective settings and the settings of all contained components.
 * @author Frank
 * @param <PL>
 *      the type of the PerspectiveLifecycle
 * @param <PS>
 *      the type of the Perspective settings
 */
public class PerspectiveCompositeLifecycleSettings<PL extends PerspectiveLifecycle<?,?>, PS extends Settings> extends AbstractSettings {
    
    private final CompositeLifecycleSettings componentLifeycycleSettings;
    private final PerspectiveLifecycleAndSettings<PL, PS> perspectiveLifecycleAndSettings;
    
    public PerspectiveCompositeLifecycleSettings(PerspectiveLifecycleAndSettings<PL, PS> perspectiveLifecycleAndSettings, 
            CompositeLifecycleSettings componentLifeycycleSettings) {
        this.perspectiveLifecycleAndSettings = perspectiveLifecycleAndSettings;
        this.componentLifeycycleSettings = componentLifeycycleSettings;
    }

    public CompositeLifecycleSettings getComponentLifecyclesAndSettings() {
        return componentLifeycycleSettings;
    }

    public PerspectiveLifecycleAndSettings<PL, PS> getPerspectiveLifecycleAndSettings() {
        return perspectiveLifecycleAndSettings;
    }    

    public boolean hasSettings() {
        return ((perspectiveLifecycleAndSettings != null && perspectiveLifecycleAndSettings.getPerspectiveLifecycle().hasSettings()) ||
                (componentLifeycycleSettings != null && componentLifeycycleSettings.hasSettings()));
    }
}
