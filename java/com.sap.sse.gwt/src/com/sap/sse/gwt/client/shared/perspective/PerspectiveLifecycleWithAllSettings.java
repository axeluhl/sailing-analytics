package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * A utility class keeping together a {@link PerspectiveLifecycle} and all it's component settings together with the corresponding ComponentLifecycle's 
 *
 * @param <PL>
 *            the type of the perspective lifecycle
 * @param <PS>
 *            the type of the perspective own settings
 * @author Frank Mittag
 */
public class PerspectiveLifecycleWithAllSettings<PL extends PerspectiveLifecycle<PS, ?, ?>, PS extends Settings> {
    
    private final PL perspectiveLifecycle;
    private PerspectiveCompositeSettings<PS> allSettings;
    
    public PerspectiveLifecycleWithAllSettings(PL perspectiveLifecycle, PerspectiveCompositeSettings<PS> allSettings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.allSettings = allSettings;
    }

    public PL getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }

    public PS getPerspectiveSettings() {
        return allSettings.getPerspectiveAndSettings().getSettings();
    }

    public CompositeSettings getComponentSettings() {
        return allSettings;
    }
    
    public PerspectiveCompositeSettings<PS> getAllSettings() {
        return allSettings;
    }

    public void setAllSettings(PerspectiveCompositeSettings<PS> allSettings) {
        this.allSettings = allSettings;
    }
    
    public <C extends ComponentLifecycle<S,?> ,S extends Settings> S findComponentSettingsByLifecycle(C componentLifecycle) {
        ComponentIdAndSettings<S> componentAndSettings = allSettings.findComponentAndSettingsByLifecycle(componentLifecycle);
        if(componentAndSettings != null) {
            return componentAndSettings.getSettings();
        }
        return null;
    }
}
