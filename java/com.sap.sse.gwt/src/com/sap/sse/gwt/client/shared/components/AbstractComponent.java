package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    
    private ComponentTreeNodeInfo<SettingsType> componentTreeNodeInfo = new ComponentTreeNodeInfo<>(this);
    
    @Override
    public Serializable getId() {
        return getLocalizedShortName();
    }
    
    @Override
    public ComponentTreeNodeInfo<SettingsType> getComponentTreeNodeInfo() {
        return componentTreeNodeInfo;
    }
}
