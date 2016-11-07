package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * An abstract base class for perspective lifecycle's. Subclasses need to add the {@link ComponentLifecycle} instances
 * for their child components to the {@link #componentLifecycles} structure in their constructor.
 * 
 * @param <PS>
 *            the perspective settings type
 * @author Frank Mittag
 *
 */
public abstract class AbstractPerspectiveLifecycle<PS extends Settings> implements PerspectiveLifecycle<PS> {

    protected final List<ComponentLifecycle<?,?>> componentLifecycles;
    
    public AbstractPerspectiveLifecycle() {
        componentLifecycles = new ArrayList<>();
    }
    
    public PerspectiveCompositeTabbedSettingsDialogComponent<PS> getSettingsDialogComponent(PerspectiveCompositeSettings<PS> settings) {
        PerspectiveLifecycleWithAllSettings<?, PS> perspectiveLifecycleWithAllSettings = new PerspectiveLifecycleWithAllSettings<>(this, settings); 
        return new PerspectiveCompositeTabbedSettingsDialogComponent<PS>(perspectiveLifecycleWithAllSettings);
    }
    
    @Override
    public PerspectiveCompositeSettings<PS> createDefaultSettings() {
        PS perspectiveOwnSettings = createPerspectiveOwnDefaultSettings();
        return new PerspectiveCompositeSettings<>(perspectiveOwnSettings, getComponentIdsAndDefaultSettings().getSettingsPerComponentId());
    }
    
    @Override
    public PerspectiveCompositeSettings<PS> cloneSettings(PerspectiveCompositeSettings<PS> settings) {
        return new PerspectiveCompositeSettings<>(this.clonePerspectiveOwnSettings(settings.getPerspectiveOwnSettings()), getComponentIdsAndClonedSettings(settings).getSettingsPerComponentId());
    }
    
    protected CompositeSettings getComponentIdsAndClonedSettings(PerspectiveCompositeSettings<PS> settings) {
        Map<String, Settings> componentIdsAndSettings = new HashMap<>();
        for (ComponentLifecycle<?,?> componentLifecycle : componentLifecycles) {
            componentIdsAndSettings.put(componentLifecycle.getComponentId(), cloneChildComponentSettings(componentLifecycle, settings));
        }
        CompositeSettings compositeSettings = new CompositeSettings(componentIdsAndSettings);
        return compositeSettings;
    }
    
    protected CompositeSettings getComponentIdsAndDefaultSettings() {
        Map<String, Settings> componentIdsAndSettings = new HashMap<>();
        for (ComponentLifecycle<?,?> componentLifecycle : componentLifecycles) {
            componentIdsAndSettings.put(componentLifecycle.getComponentId(), componentLifecycle.createDefaultSettings());
        }
        CompositeSettings compositeSettings = new CompositeSettings(componentIdsAndSettings);
        return compositeSettings;
    }

    public Iterable<ComponentLifecycle<?,?>> getComponentLifecycles() {
        return componentLifecycles;
    }
    
    private static<S extends Settings> S cloneChildComponentSettings(ComponentLifecycle<S, ?> childComponentLifecycle, PerspectiveCompositeSettings<?> settings) {
        @SuppressWarnings("unchecked")
        S childSettings = (S) settings.findSettingsByComponentId(childComponentLifecycle.getComponentId());
        return childComponentLifecycle.cloneSettings(childSettings);
    }

}
