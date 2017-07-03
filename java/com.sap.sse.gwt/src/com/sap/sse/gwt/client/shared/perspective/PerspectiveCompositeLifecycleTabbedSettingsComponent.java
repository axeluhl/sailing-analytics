package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A component that contains a collection of settings components in a tabbed panel.
 *  
 * @author Frank (c5163874)
 * @param <PL>
 *      the {@link PerspectiveLifecycle} type
 * @param <PS>
 *      the {@link Perspective} settings type
 */
public class PerspectiveCompositeLifecycleTabbedSettingsComponent<PL extends PerspectiveLifecycle<PS>, PS extends Settings>
    extends AbstractComponent<PerspectiveCompositeSettings<PS>> {
    
    private final String title;
    private PerspectiveCompositeSettings<PS> perspectiveCompositeSettings;
    private PL lifecycle;
    
    public PerspectiveCompositeLifecycleTabbedSettingsComponent(Component<?> parent, ComponentContext<?> context,
            PL lifecycle,
            PerspectiveCompositeSettings<PS> settings) {
        this(parent, context, lifecycle, settings, null);
    }

    public PerspectiveCompositeLifecycleTabbedSettingsComponent(Component<?> parent, ComponentContext<?> context,
            PL lifecycle,
            PerspectiveCompositeSettings<PS> settings,
            String title) {
        super(parent, context);
        this.lifecycle = lifecycle;
        this.perspectiveCompositeSettings = settings;
        this.title = title;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<PerspectiveCompositeSettings<PS>> getSettingsDialogComponent(PerspectiveCompositeSettings<PS> settings) {
        return new PerspectiveCompositeTabbedSettingsDialogComponent<PS>(lifecycle, settings);
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
        return lifecycle.getLocalizedShortName();
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

    @Override
    public String getId() {
        return "PerspectiveCompositeLifecycleTabbedSettingsComponentFor" + lifecycle.getComponentId();
    }
}