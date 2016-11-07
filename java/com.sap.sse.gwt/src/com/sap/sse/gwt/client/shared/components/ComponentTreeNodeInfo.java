package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.Perspective;

public class ComponentTreeNodeInfo<SettingsType extends Settings> {
    
    private Perspective<? extends Settings> parentPerspective = null;
    private AbstractComponentContextWithSettingsStorage<?, ?> componentContext = null;
    
    public void setParentPerspective(Perspective<? extends Settings> parentPerspective) {
        this.parentPerspective = parentPerspective;
    }
    
    public Perspective<? extends Settings> getParentPerspective() {
        return parentPerspective;
    }
    
    public void setComponentContext(AbstractComponentContextWithSettingsStorage<?, ?> componentContext) {
        this.componentContext = componentContext;
    }
    
    public AbstractComponentContextWithSettingsStorage<?, ?> getComponentContext() {
        return componentContext;
    }
    
}
