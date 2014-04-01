package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractLazyComponent<SettingsType> extends LazyPanel implements LazyComponent<SettingsType> {

    @Override
    public Widget getEntryWidget() {
        ensureWidget();
        return getWidget();
    }
}
