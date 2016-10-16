package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.Perspective;

public class ComponentTreeNodeInfo<SettingsType extends Settings> {
    
    private Perspective<? extends Settings> parentPerspective = null;
    private ComponentContext componentContext = null;
    
    public void setParentPerspective(Perspective<? extends Settings> parentPerspective) {
        this.parentPerspective = parentPerspective;
    }
    
    public Perspective<? extends Settings> getParentPerspective() {
        return parentPerspective;
    }
    
    public void setComponentContext(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }
    
    public ComponentContext getComponentContext() {
        return componentContext;
    }
    
}
