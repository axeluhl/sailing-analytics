package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * An abstract base class for perspectives with a widget.
 * @author Frank
 *
 */
public abstract class AbstractPerspectiveComposite<SettingsType extends Settings> extends Composite implements Perspective<SettingsType> {

    protected final List<Component<?>> components;
    
    public AbstractPerspectiveComposite() {
        this.components = new ArrayList<>();
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
        ComponentAndSettingsPair<ComponentSettingsType> result = null;
        if(component.hasSettings()) {
            result = new ComponentAndSettingsPair<ComponentSettingsType>(component, component.getSettings());
        }
        return result;
    }

    @Override
    public void updateSettingsOfComponents(CompositeSettings newSettings) {
        for (ComponentAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateSettings(componentAndSettings);
        }
    }

    private <ComponentSettingsType extends Settings> void updateSettings(ComponentAndSettingsPair<ComponentSettingsType> componentAndSettings) {
        Component<ComponentSettingsType> component = componentAndSettings.getComponent();
        component.updateSettings(componentAndSettings.getSettings());
    }

    @Override
    public Widget getEntryWidget() {
        return this.asWidget();
    }

    public List<Component<?>> getComponents() {
        return components;
    }
}
