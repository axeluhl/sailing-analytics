package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    private Component<?> parentComponent;
    private ComponentContext<?> componentContext;

    public AbstractComponent(Component<?> parent, ComponentContext<?> componentContext) {
        this.parentComponent = parent;
        this.componentContext = componentContext;
    }
    
    @Override
    public Component<?> getParentComponent() {
        return parentComponent;
    }

    @Override
    public final ComponentContext<?> getComponentContext() {
        return componentContext;
    }
}
