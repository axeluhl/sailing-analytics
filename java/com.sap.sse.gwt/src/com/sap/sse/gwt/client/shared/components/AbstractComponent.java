package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    private Component<?> parentComponent;
    private ComponentContext<?> context;

    public AbstractComponent(Component<?> parent, ComponentContext<?> context) {
        this.parentComponent = parent;
        this.context = context;
    }
    
    @Override
    public Component<?> getParentComponent() {
        return parentComponent;
    }

    @Override
    public ArrayList<String> getPath() {
        return ComponentPathDeterminer.determinePath(this);
    }

    @Override
    public final ComponentContext<?> getComponentContext() {
        return context;
    }
}
