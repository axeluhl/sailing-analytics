package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public abstract class AbstractPerspectiveWithoutSettings extends AbstractPerspective<AbstractSettings> {

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public Widget getEntryWidget() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
    }

    @Override
    public boolean hasSettings() {
        return false;
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
