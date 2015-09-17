package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;

/**
 * A component, that contains a collection of settings components in a tabbed panel.
 *  
 * @author Axel Uhl (d043530), Lennart Hensler (D054527)
 */
public class CompositeSettingsComponent implements Component<CompositeSettings> {
    
    private final Iterable<Component<?>> components;
    private final String title;
    
    public CompositeSettingsComponent(Iterable<Component<?>> components) {
        this(components, null);
    }

    public CompositeSettingsComponent(Iterable<Component<?>> components, String title) {
        this.components = components;
        this.title = title;
    }

    @Override
    public boolean hasSettings() {
        for (Component<?> component : components) {
            if (component.hasSettings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SettingsDialogComponent<CompositeSettings> getSettingsDialogComponent() {
        return new CompositeSettingsDialogComponent(components);
    }

    @Override
    public void updateSettings(CompositeSettings newSettings) {
        for (CompositeSettings.ComponentAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateSettings(componentAndSettings);
        }
    }

    private <SettingsType extends Settings> void updateSettings(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
        componentAndSettings.getA().updateSettings(componentAndSettings.getB());
    }

    @Override
    public String getLocalizedShortName() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Component<?> component : components) {
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