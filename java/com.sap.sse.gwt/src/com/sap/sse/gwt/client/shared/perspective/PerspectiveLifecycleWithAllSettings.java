package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;

/**
 * A class for keeping together all settings of a PerspectiveLifecycle including it's components
 *
 * @param <P>
 *            the type of the perspective lifecycle
 * @param <S>
 *            the type of the perspective settings
 * @author Frank Mittag
 */
public class PerspectiveLifecycleWithAllSettings<P extends PerspectiveLifecycle<?,S,?>, S extends Settings> {
    private final P perspectiveLifecycle;
    private PerspectiveCompositeLifecycleSettings<P,S> allSettings;
    
    public PerspectiveLifecycleWithAllSettings(P perspectiveLifecycle, PerspectiveCompositeLifecycleSettings<P,S> allSettings) {
        this.perspectiveLifecycle = perspectiveLifecycle;
        this.allSettings = allSettings;
    }

    public P getPerspectiveLifecycle() {
        return perspectiveLifecycle;
    }

    public S getPerspectiveSettings() {
        return allSettings.getPerspectiveLifecycleAndSettings().getSettings();
    }

    public CompositeLifecycleSettings getComponentSettings() {
        return allSettings.getComponentLifecyclesAndSettings();
    }
    
    public PerspectiveCompositeLifecycleSettings<P,S> getAllSettings() {
        return allSettings;
    }

    public void setAllSettings(PerspectiveCompositeLifecycleSettings<P,S> allSettings) {
        this.allSettings = allSettings;
    }
}
