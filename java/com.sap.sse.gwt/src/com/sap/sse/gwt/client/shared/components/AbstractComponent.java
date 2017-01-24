package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    private Component<?> parentComponent;

    public AbstractComponent(Component<?> parent) {
        this.parentComponent = parent;
    }
    
    @Override
    public Component<?> getParentComponent() {
        return parentComponent;
    }

    @Override
    public ArrayList<String> getPath() {
        return ComponentPathDeterminer.determinePath(this);
    }
}
