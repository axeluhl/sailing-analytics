package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * An abstract base class for perspectives.
 * @author Frank
 *
 */
public abstract class AbstractPerspective<SettingsType extends Settings> implements Perspective<SettingsType> {

    protected final List<Component<?>> components;
    
    public AbstractPerspective(Collection<Component<?>> components) {
        this.components = new ArrayList<>(components);
    }
    
    @Override 
    public CompositeSettings getSettingsOfComponents() {
        Collection<ComponentAndSettings<?>> settings = new HashSet<>();
        for (Component<?> component : components) {
            ComponentAndSettings<?> componentAndSettings = getComponentAndSettings(component);
            if (componentAndSettings != null) {
                settings.add(componentAndSettings);
            }
        }
        return new CompositeSettings(settings);
    }
    
    private <ComponentSettingsType extends Settings> ComponentAndSettings<ComponentSettingsType> getComponentAndSettings(Component<ComponentSettingsType> component) {
        ComponentAndSettings<ComponentSettingsType> result = null;
        if(component.hasSettings()) {
            result = new ComponentAndSettings<ComponentSettingsType>(component, component.getSettings());
        }
        return result;
    }

    @Override
    public void updateSettingsOfComponents(CompositeSettings newSettings) {
        for (ComponentAndSettings<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateSettings(componentAndSettings);
        }
    }

    private <ComponentSettingsType extends Settings> void updateSettings(ComponentAndSettings<ComponentSettingsType> componentAndSettings) {
        Component<ComponentSettingsType> component = componentAndSettings.getComponent();
        component.updateSettings(componentAndSettings.getSettings());
    }
}
