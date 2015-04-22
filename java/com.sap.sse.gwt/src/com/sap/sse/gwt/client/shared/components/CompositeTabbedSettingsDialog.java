package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.CompositeSettings.ComponentAndSettingsPair;

/**
 * A composite settings dialog that combines the settings of several {@link Component}s, providing a tab
 * for each component.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompositeTabbedSettingsDialog extends SettingsDialog<CompositeSettings> {
    private final Map<Component<?>, SettingsDialogComponent<?>> settingsDialogComponents;
    
    public CompositeTabbedSettingsDialog(StringMessages stringConstants, final Component<?>... components) {
        super(new Component<CompositeSettings>() {
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

            private <SettingsType extends AbstractSettings> void updateSettings(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
                componentAndSettings.getA().updateSettings(componentAndSettings.getB());
            }

            @Override
            public String getLocalizedShortName() {
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
        }, stringConstants);
        settingsDialogComponents = new HashMap<Component<?>, SettingsDialogComponent<?>>();
        for (Component<?> component : components) {
            if (component.hasSettings()) {
                settingsDialogComponents.put(component, component.getSettingsDialogComponent());
            }
        }
    }
    
}
