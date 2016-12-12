package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.common.settings.Settings;

public abstract class AbstractCompositeComponent<SettingsType extends Settings> extends Composite implements Component<SettingsType> {

    private ComponentTreeNodeInfo componentTreeNodeInfo = new ComponentTreeNodeInfo();
    
    @Override
    public ComponentTreeNodeInfo getComponentTreeNodeInfo() {
        return componentTreeNodeInfo;
    }
}
