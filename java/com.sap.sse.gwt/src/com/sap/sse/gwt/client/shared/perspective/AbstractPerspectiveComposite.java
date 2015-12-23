package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentLifecycleAndSettingsPair;

/**
 * An abstract base class for perspectives with a widget.
 * @author Frank
 *
 */
public abstract class AbstractPerspectiveComposite<SettingsType extends Settings> extends Composite implements Perspective<SettingsType> {

    protected final List<ComponentLifecycle<?,?,?>> components;
    
    public AbstractPerspectiveComposite() {
        components = new ArrayList<ComponentLifecycle<?,?,?>>();
    }
    
    @Override 
    public CompositeSettings getSettingsOfComponents() {
        Collection<ComponentLifecycleAndSettingsPair<?>> settings = new HashSet<>();
        for (ComponentLifecycle<?,?,?> component : components) {
            ComponentLifecycleAndSettingsPair<?> componentAndSettings = getComponentLifecycleAndSettings(component);
            if (componentAndSettings != null) {
                settings.add(componentAndSettings);
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
        for (CompositeSettings.ComponentLifecycleAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponentLifecycle()) {
            updateSettings(componentAndSettings);
        }
    }

    private <ComponentSettingsType extends Settings> void updateSettings(ComponentLifecycleAndSettingsPair<ComponentSettingsType> componentLifecycleAndSettings) {
        ComponentLifecycle<?, ComponentSettingsType, ?> componentLifecycle = componentLifecycleAndSettings.getA();
        if(componentLifecycle.getComponent() != null) {
            componentLifecycle.getComponent().updateSettings(componentLifecycleAndSettings.getB());
        } else {
            // ??? store in lifecycle
        }
    }
    
    @Override
    public Iterable<ComponentLifecycle<?,?,?>> getComponentLifecycles() {
        return components;
    }

    @Override
    public Widget getEntryWidget() {
        return this.asWidget();
    }
}
