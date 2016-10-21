package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.AbstractSettings;

public abstract class AbstractLazyComponent<SettingsType extends AbstractSettings> extends LazyPanel implements LazyComponent<SettingsType> {

    private ComponentTreeNodeInfo<SettingsType> componentTreeNodeInfo = new ComponentTreeNodeInfo<>();
    
    @Override
    public String getId() {
        return getLocalizedShortName();
    }
    
    @Override
    public ComponentTreeNodeInfo<SettingsType> getComponentTreeNodeInfo() {
        return componentTreeNodeInfo;
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

}
