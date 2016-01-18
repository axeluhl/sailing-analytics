package com.sap.sse.gwt.client.shared.perspective;

import java.util.Collection;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public abstract class AbstractPerspectiveWithoutSettings extends AbstractPerspective<AbstractSettings> {

    public AbstractPerspectiveWithoutSettings(Collection<Component<?>> components) {
        super(components);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public AbstractSettings getSettings() {
        return null;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }
}
