package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
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
    extends AbstractComponent<PerspectiveCompositeSettings<PS>> {
    
    private PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings;
    private final String title;
    private PerspectiveCompositeSettings<PS> perspectiveCompositeSettings;
    
    public PerspectiveCompositeLifecycleTabbedSettingsComponent(PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings) {
        this(perspectiveLifecycleWithAllSettings, null);
    }

    public PerspectiveCompositeLifecycleTabbedSettingsComponent(PerspectiveLifecycleWithAllSettings<PL, PS> perspectiveLifecycleWithAllSettings, String title) {
        this.perspectiveLifecycleWithAllSettings = perspectiveLifecycleWithAllSettings;
        this.perspectiveCompositeSettings = perspectiveLifecycleWithAllSettings.getAllSettings(); 
        this.title = title;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<PerspectiveCompositeSettings<PS>> getSettingsDialogComponent() {
        return new PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<PS>(perspectiveLifecycleWithAllSettings);
    }

    @Override
    public PerspectiveCompositeSettings<PS> getSettings() {
        return perspectiveCompositeSettings;
    }
 
    @Override
    public void updateSettings(PerspectiveCompositeSettings<PS> newSettings) {
        this.perspectiveCompositeSettings = newSettings;
    }
    
    @Override
    public String getLocalizedShortName() {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        return perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle().getLocalizedShortName();
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