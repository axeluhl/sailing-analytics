package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.Perspective;

public class ComponentTreeNodeInfo<SettingsType extends Settings> {
    
    private Perspective<? extends Settings> parentPerspective = null;
    private final Component<SettingsType> component;
    
    public ComponentTreeNodeInfo(Component<SettingsType> component) {
        this.component = component;
    }
    
    public void setParentPerspective(Perspective<? extends Settings> parentPerspective) {
        this.parentPerspective = parentPerspective;
    }
    
    public Perspective<? extends Settings> getParentPerspective() {
        return parentPerspective;
    }
    
    public void makeSettingsDefault(SettingsType newDefaultSettings) {
        if(parentPerspective != null) {
            parentPerspective.childComponentDefaultSettingsChanged(component, newDefaultSettings);
        } else {
            //TODO cannot be?
            throw new IllegalStateException("Component must belong to a perspective when updating default settings");
        }
    }
}
