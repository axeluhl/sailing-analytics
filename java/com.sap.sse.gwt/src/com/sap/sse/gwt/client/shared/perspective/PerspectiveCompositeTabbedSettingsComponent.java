package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettingsPair;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A component, that contains the settings of a perspective and all it's child components in a tabbed panel.
 *  
 * @author Frank Mittag (c5163874)
 */
public class PerspectiveCompositeTabbedSettingsComponent implements Component<PerspectiveCompositeSettings> {
    
    private final Iterable<Component<?>> components;
    private final Perspective<?> perspective;
    private final String title;
    
    public PerspectiveCompositeTabbedSettingsComponent(Perspective<?> perspective) {
        this(perspective, null);
    }

    public PerspectiveCompositeTabbedSettingsComponent(Perspective<?> perspective, String title) {
        this.perspective = perspective;
        this.components = perspective.getComponents();
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
    public SettingsDialogComponent<PerspectiveCompositeSettings> getSettingsDialogComponent() {
        return new PerspectiveCompositeTabbedSettingsDialogComponent(perspective);
    }

    @Override
    public PerspectiveCompositeSettings getSettings() {
        return null;
    }
 
    @Override
    public void updateSettings(PerspectiveCompositeSettings newSettings) {
        updatePerspectiveSettings(newSettings.getPerspectiveSettings());
        for (ComponentAndSettingsPair<?> componentAndSettings : newSettings.getSettingsPerComponent()) {
            updateComponentSettings(componentAndSettings);
        }
    }

    private <SettingsType extends Settings> void updatePerspectiveSettings(PerspectiveAndSettingsPair<SettingsType> perspectiveAndSettings) {
        perspectiveAndSettings.getComponent().updateSettings(perspectiveAndSettings.getSettings());
    }

    private <SettingsType extends Settings> void updateComponentSettings(ComponentAndSettingsPair<SettingsType> componentAndSettings) {
        componentAndSettings.getComponent().updateSettings(componentAndSettings.getSettings());
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
