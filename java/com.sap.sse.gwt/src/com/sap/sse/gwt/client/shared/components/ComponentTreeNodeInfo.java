package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.Perspective;

/**
 * Contains information required by {@link ComponentContext} and {@link SettingsDialog} in one place.
 * There is one-to-one relationship between instances of a {@link Component} and {@link ComponentTreeNodeInfo}.
 * This class exists due to multiple abstract base implementations for {@link Component} and the wish
 * to reduce duplicate code.
 * 
 * @author Vladislav Chumak
 *
 */
public class ComponentTreeNodeInfo {
    
    /**
     * The parent perspective of the current component or {@code null} if there isn't any parent perspective.
     */
    private Perspective<? extends Settings> parentPerspective = null;
    
    /**
     * The {@link ComponentContext} maintaining the default settings of the whole component/perspective tree.
     */
    private ComponentContext<?, ?> componentContext = null;
    
    /**
     * @param parentPerspective The parent perspective of the component corresponding to this instance
     * @see #parentPerspective
     */
    public void setParentPerspective(Perspective<? extends Settings> parentPerspective) {
        this.parentPerspective = parentPerspective;
    }
    
    /**
     * 
     * @return The parent perspective of the component corresponding to this instance, or {@code null} if there
     * isn't any parent perspective containing this component
     */
    public Perspective<? extends Settings> getParentPerspective() {
        return parentPerspective;
    }
    
    /**
     * Sets the {@link ComponentContext} maintaining the default settings of the whole component/perspective tree.
     * 
     * @param componentContext
     */
    public void setComponentContext(ComponentContext<?, ?> componentContext) {
        this.componentContext = componentContext;
    }
    
    /**
     * Gets the {@link ComponentContext} maintaining the default settings of the whole component/perspective tree.
     * It would be nice to have this method to never return a {@code null}, but unfortunately it is not the current
     * state of the art.
     * 
     * @return
     */
    public ComponentContext<?, ?> getComponentContext() {
        return componentContext;
    }
    
}
