package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractComponent<SettingsType extends Settings> implements Component<SettingsType> {
    
    private ComponentTreeNodeInfo componentTreeNodeInfo = new ComponentTreeNodeInfo();
    
    @Override
    public ComponentTreeNodeInfo getComponentTreeNodeInfo() {
        return componentTreeNodeInfo;
    }
}
