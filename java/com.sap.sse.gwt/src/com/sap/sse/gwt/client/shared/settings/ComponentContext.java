package com.sap.sse.gwt.client.shared.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * Manages all default/initial settings of a component/perspective and its subcomponents if there are any. It supplies
 * components with initial settings and determines whether the settings of components are storable or not. The settings
 * can be stored with different scopes. The scope determines the influence of the stored settings on the initial
 * settings of the same component which is used to present a context (e.g. event or race). Currently, there are two
 * basic scopes which can be used for settings storage:
 * <dl>
 * <dt>Document Settings</dt>
 * <dd>The settings are stored only for the current context (e.g. a race presented in a RaceBoard). This settings are
 * not considered in any other context. See
 * {@link #storeSettingsForContext(Component, Settings, OnSettingsStoredCallback)}</dd>
 * 
 * <dt>User Settings</dt>
 * <dd>The settings are stored for a component/perspective context-independently and may effect a different context
 * presented by the same perspective/component, e.g. a different race in a RaceBoard. See
 * {@link #makeSettingsDefault(Component, Settings, OnSettingsStoredCallback)}</dd>
 * </dl>
 * A {@link SettingsStorageManager} is used as a persistence layer for settings. When initial settings are loaded,
 * {@link SettingsBuildingPipeline} is used to construct the settings object based on the data from the underlying
 * settings store. An example of such a settings construction might look as follows:
 * <ol>
 * <li>Get "System Default" settings by means of component lifecycles (see
 * {@link ComponentLifecycle#createDefaultSettings()})</li>
 * <li>Override the settings by values which are stored explicitly in the "User Settings"</li>
 * <li>Override the settings by values which are stored explicitly in the "Document Settings"</li>
 * <li>Override the settings by values which are set over URL</li>
 * </ol>
 * The whole settings construction pipeline, as well as {@link SettingsStorageManager} are customizable. For more
 * information and possibilities see the existing implementations.
 * 
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public interface ComponentContext<S extends Settings> {

    /**
     * Stores the provided {@link Settings} of the provided {@link Component} as User Settings which are
     * context-independent. Make sure to call this method only when {@link #isStorageSupported(Component)} method
     * returns {@code true} for the provided {@link Component}.
     * 
     * @param component
     *            The component which corresponds to the provided {@link Settings}
     * @param newDefaultSettings
     *            The {@link Settings} to be stored
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    <CS extends Settings> void makeSettingsDefault(Component<CS> component, CS newDefaultSettings,
            OnSettingsStoredCallback onSettingsStoredCallback);

    /**
     * Stores the provided {@link Settings} of the provided {@link Component} as Document Settings for the current
     * context. Make sure to call this method only when {@link #isStorageSupported(Component)} method returns
     * {@code true} for the provided {@link Component}.
     * 
     * @param component
     *            The component which corresponds to the provided {@link Settings}
     * @param newSettings
     *            The {@link Settings} to be stored
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    <CS extends Settings> void storeSettingsForContext(Component<CS> component, CS newSettings,
            OnSettingsStoredCallback onSettingsStoredCallback);

    /**
     * Gets the {@link ComponentLifecycle} of the root component managed by this {@link ComponentContext}.
     * 
     * @return The {@link ComponentLifecycle} of the root component
     */
    ComponentLifecycle<S> getRootLifecycle();

    /**
     * Checks whether the {@link Settings} of the provided {@link Component} are storable and whether the underlying
     * implementation supports {@link #makeSettingsDefault(Component, Settings)} calls for it.
     * 
     * @param component
     *            The component with potentially storable settings
     * @return {@code true} if the settings of the component are storable <b>AND</b> the {@link ComponentContext}
     *         implementation has settings storage support
     */
    boolean isStorageSupported(Component<?> component);

    /**
     * Gets initial settings and passes these settings to the provided callback. The provided callback gets called when
     * initial/default settings are available. Depending on implementation the callback may be called after a delay,
     * e.g. after the settings have been retrieved from a server, or immediately, e.g. when the implementation does not
     * query information from server. This method produces no side-effects if it gets called multiple times.
     * 
     * @param settingsReceiverCallback
     *            The callback which supplies the caller with initial settings
     */
    void getInitialSettings(OnSettingsLoadedCallback<S> onInitialSettingsLoaded);

    /**
     * Gets initial settings of the provided component and passes these settings to the provided callback. The provided
     * callback gets called when initial/default settings are available. Depending on implementation the callback may be
     * called after a delay, e.g. after the settings have been retrieved from a server, or immediately, e.g. when the
     * implementation does not query information from server. This method produces no side-effects if it gets called
     * multiple times.
     * 
     * @param component
     *            The component of which the settings should be retrieved
     * @param settingsReceiverCallback
     *            The callback which supplies the caller with initial settings
     */
    <CS extends Settings> void getInitialSettingsForComponent(final Component<CS> component,
            final OnSettingsLoadedCallback<CS> callback);

    /**
     * Releases all resources and listener registrations acquired by this instance. You must use this method, if
     * instances of {@link ComponentContext} are created AND destroyed dynamically during runtime.
     */
    void dispose();

    /**
     * Removes usersettings onrequest
     */
    <CS extends Settings> void resetSettingsToDefault(Component<CS> component, CS newSettings,
            OnSettingsLoadedCallback<CS> onSettingsStoredCallback);

}
