package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentLifecycleAndSettingsPair;

/**
 * A component, that contains a collection of settings components in a tabbed panel.
 *  
 * @author Axel Uhl (d043530), Lennart Hensler (D054527)
 */
public class CompositeTabbedSettingsComponent implements Component<CompositeSettings> {
    
    private final Iterable<ComponentLifecycle<?,?,?>> components;
    private final String title;
    
    public CompositeTabbedSettingsComponent(Iterable<ComponentLifecycle<?,?,?>> components) {
        this(components, null);
    }

    public CompositeTabbedSettingsComponent(Iterable<ComponentLifecycle<?,?,?>> components, String title) {
        this.components = components;
        this.title = title;
    }

    @Override
    public boolean hasSettings() {
        for (ComponentLifecycle<?,?,?> component : components) {
            if (component.hasSettings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SettingsDialogComponent<CompositeSettings> getSettingsDialogComponent() {
        return new CompositeTabbedSettingsDialogComponent(components);
    }

    @Override
    public CompositeSettings getSettings() {
        return null;
    }
 
    @Override
    public void updateSettings(CompositeSettings newSettings) {
        for (CompositeSettings.ComponentLifecycleAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponentLifecycle()) {
            updateSettings(componentAndSettings);
        }
    }

    private <SettingsType extends Settings> void updateSettings(ComponentLifecycleAndSettingsPair<SettingsType> componentLifecycleAndSettings) {
        ComponentLifecycle<?, SettingsType, ?> componentLifecycle = componentLifecycleAndSettings.getA();
        if(componentLifecycle.getComponent() != null) {
            componentLifecycle.getComponent().updateSettings(componentLifecycleAndSettings.getB());
        } else {
            // ??? store in lifecycle
        }
    }

    @Override
    public String getLocalizedShortName() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (ComponentLifecycle<?,?,?> component : components) {
                if (first) {
                    first = false;
                } else {
                    result.append(" / ");
                }
                result.append(component.getLocalizedShortName());
            }
            return result.toString();
        }
    }

    @Override
    public Widget getEntryWidget() {
        throw new RuntimeException("Virtual composite component doesn't have a widget of its own");
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
        throw new RuntimeException("Virtual composite component doesn't know how to make itself visible");
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }   
}