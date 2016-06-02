package com.sap.sse.gwt.client.shared.perspective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * An abstract base class for perspective lifecycle's.
 * @param <PS>
 *            the perspective settings type
 * @param <PCS>
 *            the perspective composite settings type
 * @param <SDP>
 *            the settings dialog component type 
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveLifecycle<PS extends Settings, PCS extends PerspectiveCompositeSettings<PS>, 
        SDP extends SettingsDialogComponent<PCS>> implements PerspectiveLifecycle<PS, PCS, SDP> {

    protected final List<ComponentLifecycle<?,?>> componentLifecycles;
    
    public AbstractPerspectiveLifecycle() {
        componentLifecycles = new ArrayList<>();
    }
    
    public PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<PS> getSettingsDialogComponent(PerspectiveCompositeSettings<PS> settings) {
        PerspectiveLifecycleWithAllSettings<?, PS> perspectiveLifecycleWithAllSettings = getPerspectiveLifecycleWithAllSettings(settings); 
        
        return new PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<PS>(perspectiveLifecycleWithAllSettings);
    }

    protected PerspectiveLifecycleWithAllSettings<?, PS> getPerspectiveLifecycleWithAllSettings(PerspectiveCompositeSettings<PS> settings) {
        // collect the component lifecycle's for contained components and combine them with the corresponding settings
        // TODO: Take the settings from the settings parameter
        CompositeSettings componentAndDefaultSettings = getComponentIdsAndDefaultSettings();
        PS perspectiveSettings = settings.getPerspectiveAndSettings().getSettings();
        PerspectiveIdAndSettings<PS> perspectiveIdAndSettings = new PerspectiveIdAndSettings<>(getComponentId(), perspectiveSettings);
        PerspectiveCompositeSettings<PS> perspectiveCompositeSettings =
                new PerspectiveCompositeSettings<>(perspectiveIdAndSettings, componentAndDefaultSettings.getSettingsPerComponentId());
        
        return new PerspectiveLifecycleWithAllSettings<>(this, perspectiveCompositeSettings); 
    }
    
    protected CompositeSettings getComponentIdsAndDefaultSettings() {
        List<ComponentIdAndSettings<?>> componentIdsAndSettings = new ArrayList<>();
        for (ComponentLifecycle<?,?> componentLifecycle : componentLifecycles) {
            componentIdsAndSettings.add(createComponentIdAndSettings(componentLifecycle));
        }
        CompositeSettings compositeSettings = new CompositeSettings(componentIdsAndSettings);
        return compositeSettings;
    }

    private <C extends ComponentLifecycle<S,?>, S extends Settings> ComponentIdAndSettings<S> createComponentIdAndSettings(C componentLifecycle) {
        S defaultSettings = componentLifecycle.createDefaultSettings();
        ComponentIdAndSettings<S> componentLifecycleAndSettings = new ComponentIdAndSettings<>(componentLifecycle.getComponentId(), defaultSettings);
        return componentLifecycleAndSettings;
    }
    
    public Iterable<ComponentLifecycle<?,?>> getComponentLifecycles() {
        return componentLifecycles;
    }

    @Override
    public Serializable getComponentId() {
        return getLocalizedShortName();
    }
}
