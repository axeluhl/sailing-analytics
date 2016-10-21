package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    
    private ComponentTreeNodeInfo<SettingsType> componentTreeNodeInfo = new ComponentTreeNodeInfo<>();
    
    @Override
    public String getId() {
        return getLocalizedShortName();
    }
    
    @Override
    public ComponentTreeNodeInfo<SettingsType> getComponentTreeNodeInfo() {
        return componentTreeNodeInfo;
    }
}
