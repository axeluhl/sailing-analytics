package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractLazyComponent<SettingsType extends Settings> extends LazyPanel implements LazyComponent<SettingsType> {
    private Component<?> parent;
    private ComponentContext<?> componentContext;

    public AbstractLazyComponent(Component<?> parent, ComponentContext<?> componentContext) {
        this.parent = parent;
        this.componentContext = componentContext;
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
    public ComponentContext<?> getComponentContext() {
        return componentContext;
    }

}
