package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.common.settings.Settings;

public abstract class AbstractCompositeComponent<SettingsType extends Settings> extends Composite implements Component<SettingsType> {

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
