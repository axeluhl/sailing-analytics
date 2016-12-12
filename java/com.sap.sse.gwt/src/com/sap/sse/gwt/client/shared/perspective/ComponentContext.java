package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * Manages all default settings of a component/perspective and its subcomponents if there are any.
 * It supplies components with initial settings and determines whether the settings of components are storable or not.
 * 
 * @author Vladislav Chumak
 *
 * @param <L> The {@link ComponentLifecycle} type of the root component/perspective containing all settings for itself and its subcomponents
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all settings for itself and its subcomponents
 */
public interface ComponentContext<L extends ComponentLifecycle<S, ?>, S extends Settings> {

    /**
     * Stores the {@link Settings} of the passed {@link Component} in the default component settings tree.
     * Make sure to call this method only when {@link #hasMakeCustomDefaultSettingsSupport(Component)}
     * method returns {@code true} for the passed {@link Component}.
     * 
     * @param component The component which the passed {@link Settings} correspond to
     * @param newDefaultSettings The {@link Settings} to be stored
     */
    void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings);

    /**
     * Gets the {@link ComponentLifecycle} of the root component managed by
     * this {@link ComponentContext}.
     * 
     * @return The {@link ComponentLifecycle} of the root component
     */
    L getRootLifecycle();

    /**
     * Gets the current default {@link Settings} of the root component managed
     * by this {@link ComponentContext}. The returned {@link Settings} should
     * contain all settings for the root component and its subcomponents.
     * 
     * @return The {@link Settings} of the root component
     */
    S getDefaultSettings();

    /**
     * Checks whether the {@link Settings} of the passed {@link Component} are
     * storable and whether the underlying implementation supports
     * {@link #makeSettingsDefault(Component, Settings)} calls for it.
     * 
     * @param component The component with potentially storable settings
     * @return {@code true} if the settings of the component are storable
     * <b>AND</b> the {@link ComponentContext} implementation has settings
     * storage support
     */
    boolean hasMakeCustomDefaultSettingsSupport(Component<?> component);

}
