package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.settings.Settings;

public abstract class AbstractLazyComponent<SettingsType extends Settings> extends LazyPanel implements LazyComponent<SettingsType> {

    @Override
    public Widget getEntryWidget() {
        ensureWidget();
        return getWidget();
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }
}
