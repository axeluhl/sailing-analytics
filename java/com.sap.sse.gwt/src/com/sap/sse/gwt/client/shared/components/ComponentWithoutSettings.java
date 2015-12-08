package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;

/**
 * An abstract implementation of the component interfaces indented to use for components without the need of having settings.
 * @author Frank
 *
 */
public abstract class ComponentWithoutSettings implements Component<AbstractSettings> {

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
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
