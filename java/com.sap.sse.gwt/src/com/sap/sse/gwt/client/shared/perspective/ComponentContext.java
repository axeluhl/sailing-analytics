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
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all the settings for itself and its subcomponents
 */
public interface ComponentContext<S extends Settings> {

    /**
     * Stores the {@link Settings} of the provided {@link Component} in the default component settings tree. Make sure to
     * call this method only when {@link #hasMakeCustomDefaultSettingsSupport(Component)} method returns {@code true}
     * for the passed {@link Component}.
     * 
     * @param component
     *            The component which corresponds to the provided {@link Settings} 
     * @param newDefaultSettings
     *            The {@link Settings} to be stored
     * @param onSettingsStoredCallback The callback which is called when the settings storage process finishes
     */
    void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings, OnSettingsStoredCallback onSettingsStoredCallback);

    /**
     * Gets the {@link ComponentLifecycle} of the root component managed by
     * this {@link ComponentContext}.
     * 
     * @return The {@link ComponentLifecycle} of the root component
     */
    ComponentLifecycle<S> getRootLifecycle();

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

    /**
     * Initialises this instance with initial settings and passes these settings to the provided callback.
     * The provided callback gets called when initial/default settings are available. That means,
     * if the initialisation of this instance has been already finished, the provided callback is called
     * immediately. If initialisation is not done yet, then the callback gets called when
     * the initialisation gets finished. This method produces no side-effects if it gets called multiple times.
     * 
     * @param settingsReceiverCallback The callback which supplies the caller with initial settings
     * 
     * @see #initInitialSettings()
     */
    void initInitialSettings(OnSettingsLoadedCallback<S> onInitialSettingsLoaded);

    /**
     * Initialises the instance with initial settings. The earlier this method gets called,
     * the earlier the initial settings are going to be available.
     * This method produces no side-effects if it gets called multiple times.
     * 
     * @see #initInitialSettings(OnSettingsLoadedCallback)
     */
    void initInitialSettings();
}
