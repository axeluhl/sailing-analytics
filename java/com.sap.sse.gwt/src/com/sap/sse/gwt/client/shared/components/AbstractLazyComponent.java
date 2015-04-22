package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.AbstractSettings;

public abstract class AbstractLazyComponent<SettingsType extends AbstractSettings> extends LazyPanel implements LazyComponent<SettingsType> {

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
