package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;

/**
 * A component, that contains a collection of settings components in a tabbed panel.
 *  
 * @author Frank (c5163874)
 */
public class CompositeLifecycleTabbedSettingsComponent implements Component<CompositeLifecycleSettings> {
    
    private final Iterable<ComponentLifecycle<?,?,?,?>> componentLifecycles;
    private final String title;
    
    public CompositeLifecycleTabbedSettingsComponent(Iterable<ComponentLifecycle<?,?,?,?>> componentLifecycles) {
        this(componentLifecycles, null);
    }

    public CompositeLifecycleTabbedSettingsComponent(Iterable<ComponentLifecycle<?,?,?,?>> componentLifecycles, String title) {
        this.componentLifecycles = componentLifecycles;
        this.title = title;
    }

    @Override
    public boolean hasSettings() {
        for (ComponentLifecycle<?,?,?,?> component : componentLifecycles) {
            if (component.hasSettings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SettingsDialogComponent<CompositeLifecycleSettings> getSettingsDialogComponent() {
        return new CompositeLifecycleTabbedSettingsDialogComponent(componentLifecycles);
    }

    @Override
    public CompositeLifecycleSettings getSettings() {
        return null;
    }
 
    @Override
    public void updateSettings(CompositeLifecycleSettings newSettings) {
        for (ComponentLifecycleAndSettings<?> componentAndSettings : newSettings.getSettingsPerComponentLifecycle()) {
            updateSettings(componentAndSettings);
        }
    }

    private <SettingsType extends Settings> void updateSettings(ComponentLifecycleAndSettings<SettingsType> componentLifecycleAndSettings) {
//        componentLifecycleAndSettings.getA().updateSettings(componentLifecycleAndSettings.getB());
    }

    @Override
    public String getLocalizedShortName() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (ComponentLifecycle<?,?,?,?> component : componentLifecycles) {
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