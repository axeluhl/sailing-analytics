package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public abstract class AbstractPerspectiveWithoutSettings extends AbstractPerspective<AbstractSettings> {

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
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }
}
