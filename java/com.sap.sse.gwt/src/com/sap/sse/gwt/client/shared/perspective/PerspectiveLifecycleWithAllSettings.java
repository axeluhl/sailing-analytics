package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

/**
 * A class keeping together a {@link PerspectiveLifecycle} and all it's component settings together with the corresponding ComponentLifecycle's 
 *
 * @param <PL>
 *            the type of the perspective lifecycle
 * @param <PS>
 *            the type of the perspective settings
 * @author Frank Mittag
 */
public class PerspectiveLifecycleWithAllSettings<PL extends PerspectiveLifecycle<?, ?>, PS extends Settings> {
    
    private final PL perspectiveLifecycle;
    private PerspectiveCompositeLifecycleSettings<PL, PS> allSettings;
    
    public PerspectiveLifecycleWithAllSettings(PL perspectiveLifecycle, PerspectiveCompositeLifecycleSettings<PL,PS> allSettings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.allSettings = allSettings;
    }

    public PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }

    public PS getPerspectiveSettings() {
        return allSettings.getPerspectiveLifecycleAndSettings().getSettings();
    }

    public CompositeLifecycleSettings getComponentSettings() {
        return allSettings.getComponentLifecyclesAndSettings();
    }
    
    public PerspectiveCompositeLifecycleSettings<PL,PS> getAllSettings() {
        return allSettings;
    }

    public void setAllSettings(PerspectiveCompositeLifecycleSettings<PL,PS> allSettings) {
        this.allSettings = allSettings;
    }
}
