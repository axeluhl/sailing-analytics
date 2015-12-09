package com.sap.sailing.gwt.ui.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;

/**
 * An abstract base class for perspectives.
 * @author Frank
 *
 */
public abstract class AbstractPerspective<SettingsType extends Settings> implements Perspective<SettingsType> {

    protected final List<Component<?>> components;
    
    public AbstractPerspective() {
        components = new ArrayList<Component<?>>();
    }
    
    @Override 
    public CompositeSettings getSettingsOfComponents() {
        Collection<ComponentAndSettingsPair<?>> settings = new HashSet<>();
        for (Component<?> component : components) {
            ComponentAndSettingsPair<?> componentAndSettings = getComponentAndSettings(component);
            if (componentAndSettings != null) {
                settings.add(componentAndSettings);
            }
        }
        return new CompositeSettings(settings);
    }
    
    private <ComponentSettingsType extends Settings> ComponentAndSettingsPair<ComponentSettingsType> getComponentAndSettings(Component<ComponentSettingsType> component) {
        return component.hasSettings() ? new ComponentAndSettingsPair<ComponentSettingsType>(component, component.getSettings()) : null;
    }

    @Override
    public void updateSettingsOfComponents(CompositeSettings newSettings) {
        for (CompositeSettings.ComponentAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateSettings(componentAndSettings);
        }
    }

    private <ComponentSettingsType extends Settings> void updateSettings(ComponentAndSettingsPair<ComponentSettingsType> componentAndSettings) {
        componentAndSettings.getA().updateSettings(componentAndSettings.getB());
    }
    
    @Override
    public Iterable<Component<?>> getComponents() {
        return components;
    }
}
