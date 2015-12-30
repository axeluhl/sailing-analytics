package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentLifecycleAndSettingsPair;

/**
 * An abstract base class for perspectives.
 * @author Frank
 *
 */
public abstract class AbstractPerspective<SettingsType extends Settings> implements Perspective<SettingsType> {

    protected final List<ComponentLifecycle<?,?,?>> componentLifecycles;
    
    public AbstractPerspective() {
        componentLifecycles = new ArrayList<ComponentLifecycle<?,?,?>>();
    }
    
    @Override 
    public CompositeSettings getSettingsOfComponents() {
        Collection<ComponentLifecycleAndSettingsPair<?>> settings = new HashSet<>();
        for (ComponentLifecycle<?,?,?> component : componentLifecycles) {
            ComponentLifecycleAndSettingsPair<?> componentLifecycleAndSettings = getComponentLifecycleAndSettings(component);
            if (componentLifecycleAndSettings != null) {
                settings.add(componentLifecycleAndSettings);
            }
        }
        return new CompositeSettings(settings);
    }
    
    private <ComponentSettingsType extends Settings> ComponentLifecycleAndSettingsPair<ComponentSettingsType> getComponentLifecycleAndSettings(ComponentLifecycle<?, ComponentSettingsType, ?> componentLifecycle) {
        ComponentLifecycleAndSettingsPair<ComponentSettingsType> result = null;
        if(componentLifecycle.hasSettings()) {
            ComponentSettingsType settings = componentLifecycle.getComponent() != null ? componentLifecycle.getComponent().getSettings() : componentLifecycle.createDefaultSettings();
            result = new ComponentLifecycleAndSettingsPair<ComponentSettingsType>(componentLifecycle, settings);
        }
        return result;
    }

    @Override
    public void updateSettingsOfComponents(CompositeSettings newSettings) {
        for (CompositeSettings.ComponentLifecycleAndSettingsPair<?> componentLifecycleAndSettings : newSettings.getSettingsPerComponentLifecycle()) {
            updateSettings(componentLifecycleAndSettings);
        }
    }

    private <ComponentSettingsType extends Settings> void updateSettings(ComponentLifecycleAndSettingsPair<ComponentSettingsType> componentLifecycleAndSettings) {
        ComponentLifecycle<?, ComponentSettingsType, ?> componentLifecycle = componentLifecycleAndSettings.getComponentLifecycle();
        if(componentLifecycle.getComponent() != null) {
            componentLifecycle.getComponent().updateSettings(componentLifecycleAndSettings.getSettings());
        } else {
            // ??? store in lifecycle
        }
    }
    
    @Override
    public Iterable<ComponentLifecycle<?,?,?>> getComponentLifecycles() {
        return componentLifecycles;
    }
}
