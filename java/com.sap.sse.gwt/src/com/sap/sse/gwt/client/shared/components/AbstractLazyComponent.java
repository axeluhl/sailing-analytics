package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;

public abstract class AbstractLazyComponent<SettingsType extends Settings> extends LazyPanel implements LazyComponent<SettingsType> {
    private Component<?> parent;

    public AbstractLazyComponent(Component<?> parent) {
        this.parent = parent;
    }

    @Override
    public Widget getEntryWidget() {
        ensureWidget();
        return getWidget();
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    @Override
    public Component<?> getParentComponent() {
        return parent;
    }

    @Override
    public ArrayList<String> getPath() {
        return ComponentPathDeterminer.determinePath(parent);
    }

}
