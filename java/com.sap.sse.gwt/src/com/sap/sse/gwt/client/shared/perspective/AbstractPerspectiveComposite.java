package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;

/**
 * An abstract base class for perspectives with a widget.
 * @author Frank
 *
 */
public abstract class AbstractPerspectiveComposite<SettingsType extends Settings> extends Composite implements Perspective<SettingsType> {

    protected final List<Component<?>> components;
    
    public AbstractPerspectiveComposite() {
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

    @Override
    public Widget getEntryWidget() {
        return this.asWidget();
    }
}
