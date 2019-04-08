package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * An abstract implementation of the component interfaces indented to use for components without the need of having settings.
 * @author Frank
 *
 */
public abstract class ComponentWithoutSettings extends AbstractComponent<AbstractSettings> {

    public ComponentWithoutSettings(Component<?> parent, ComponentContext<?> context) {
        super(parent, context);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent(AbstractSettings settings) {
        return null;
    }

    @Override
    public AbstractSettings getSettings() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
        // no-op
    }
}
