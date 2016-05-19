package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * A component that contains a collection of settings components in a tabbed panel.
 *  
 * @author Frank (c5163874)
 * @param <PL>
 *      the {@link PerspectiveLifecycle} type
 * @param <PS>
 *      the {@link Perspective} settings type
 */
public class PerspectiveCompositeLifecycleTabbedSettingsComponent<PL extends PerspectiveLifecycle<PS, ?,?>, PS extends Settings>
    implements Component<PerspectiveCompositeLifecycleSettings<PL,PS>> {
    
    private PerspectiveCompositeLifecycleSettings<PL, PS> compositeLifecycleSettings;
    private final String title;
    
    public PerspectiveCompositeLifecycleTabbedSettingsComponent(PerspectiveCompositeLifecycleSettings<PL,PS> compositeLifecycleSettings) {
        this(compositeLifecycleSettings, null);
    }

    public PerspectiveCompositeLifecycleTabbedSettingsComponent(PerspectiveCompositeLifecycleSettings<PL,PS> compositeLifecycleSettings, String title) {
        this.compositeLifecycleSettings = compositeLifecycleSettings;
        this.title = title;
    }

    @Override
    public boolean hasSettings() {
        return compositeLifecycleSettings.hasSettings();
    }

    @Override
    public SettingsDialogComponent<PerspectiveCompositeLifecycleSettings<PL,PS>> getSettingsDialogComponent() {
        return new PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<PL,PS>(compositeLifecycleSettings);
    }

    @Override
    public PerspectiveCompositeLifecycleSettings<PL,PS> getSettings() {
        return compositeLifecycleSettings;
    }
 
    @Override
    public void updateSettings(PerspectiveCompositeLifecycleSettings<PL,PS> newSettings) {
        this.compositeLifecycleSettings = newSettings;
    }
    
    @Override
    public String getLocalizedShortName() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (ComponentLifecycleAndSettings<?,?> component : compositeLifecycleSettings.getComponentLifecyclesAndSettings().getSettingsPerComponentLifecycle()) {
                if (first) {
                    first = false;
                } else {
                    result.append(" / ");
                }
                result.append(component.getComponentLifecycle().getLocalizedShortName());
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