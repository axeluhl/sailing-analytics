package com.sap.sse.gwt.client.shared.components;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractCompositeComponent<SettingsType extends Settings> extends Composite implements Component<SettingsType> {
    private Component<?> parentComponent;
    private ComponentContext<?> componentContext;
    private Set<VisibilityListener> visibilityListeners;
    
    public static interface VisibilityListener {
        void visibilityChanged(boolean visible);
    }

    public AbstractCompositeComponent(Component<?> parent, ComponentContext<?> componentContext) {
        this.componentContext = componentContext;
        this.visibilityListeners = new HashSet<>();
        this.parentComponent = parent;
        if (parentComponent == null) {
            GWT.log("No parent for component, validate if correct " + this.getClass().getName());
        }
        if (componentContext == null) {
            GWT.log("No context for component, validate if correct " + this.getClass().getName());
        }
    }
    
    public void addVisibilityListener(VisibilityListener listener) {
        visibilityListeners.add(listener);
    }
    
    public void removeVisibilityListener(VisibilityListener listener) {
        visibilityListeners.remove(listener);
    }
    
    @Override
    public void setVisible(boolean visible) {
        final boolean oldVisible = isVisible();
        super.setVisible(visible);
        if (oldVisible != visible) {
            notifyVisibilityListeners(visible);
        }
    }
    
    private void notifyVisibilityListeners(boolean visible) {
        for (final VisibilityListener listener : visibilityListeners) {
            listener.visibilityChanged(visible);
        }
    }

    public Component<?> getParentComponent() {
        return parentComponent;
    }

    @Override
    public ComponentContext<?> getComponentContext() {
        return componentContext;
    }

}
