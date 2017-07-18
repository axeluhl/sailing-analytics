package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.shared.settings.ComponentUtils;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.SettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;
import com.sap.sse.security.ui.client.UserService;

/**
 * Manages all the default/initial settings of a component/perspective and its subcomponents if there are any. It
 * supplies components with initial settings and determines whether the settings of components are storable or not. This
 * implementation provides settings storage support if the storable settings either implement {@link SettingsMap} or
 * {@link GenericSerializableSettings}. There are two kinds of settings which are stored separately - <b>User
 * Settings</b> (old term "Global Settings") and <b>Document Settings</b> (old term "Context specific Settings").
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
 * That means that Document Settings are stored per context (e.g. race in RaceBoard) whereas User Settings are stored
 * globally for all possible contexts (e.g. for the whole RaceBoard independently from selected race).
 * <p>
 * The underlying access to settings storage gets disabled if the URL contains {@code ignoreLocalSettings=true} flag.
 * </p>
 * 
 * 
 * @author Vladislav Chumak
 * @see ComponentContext
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 * 
 */
public class ComponentContextWithSettingsStorage<S extends Settings> extends SimpleComponentContext<S> {

    /**
     * Stored settings representations which have been already queried from persistence layer, or {@code null} if
     * previous settings retrievement has not been initiated yet.
     */
    protected StorableRepresentationOfDocumentAndUserSettings cachedSettingsRepresentation = null;

    /**
     * Manages the persistence layer of settings.
     */
    protected final SettingsStorageManager settingsStorageManager;

    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinition
     *            The definition for User Settings and Document Settings storage keys
     */
    public ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StoredSettingsLocation storageDefinition) {
        this(rootLifecycle, userService, storageDefinition, new SettingsRepresentationTransformer());
    }

    protected ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StoredSettingsLocation storageDefinition, SettingsRepresentationTransformer settingsRepresentationTransformer) {
        this(rootLifecycle, userService, storageDefinition, settingsRepresentationTransformer,
                new UserSettingsBuildingPipeline(settingsRepresentationTransformer));
    }

    protected ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StoredSettingsLocation storageDefinition, SettingsRepresentationTransformer settingsRepresentationTransformer,
            SettingsBuildingPipeline settingsBuildingPipeline) {
        super(rootLifecycle, settingsRepresentationTransformer, settingsBuildingPipeline);
        if (IgnoreLocalSettings.getIgnoreLocalSettingsFromCurrentUrl().isIgnoreLocalSettings()) {
            this.settingsStorageManager = null;
        } else {
            this.settingsStorageManager = new UserSettingsStorageManager(userService, storageDefinition);
        }
    }

    /**
     * Retrieves the initial settings and caches them for further calls of
     * {@link #getInitialSettings(OnSettingsLoadedCallback)} and
     * {@link #getInitialSettingsForComponent(Component, OnSettingsLoadedCallback)}.
     */
    public void initialize() {
        getInitialSettings(new OnSettingsLoadedCallback<S>() {
            @Override
            public void onError(Throwable caught, S fallbackDefaultSettings) {
            }

            @Override
            public void onSuccess(S settings) {
            }
        });
    }

    /**
     * Gets initial settings and passes these settings to the provided callback. The provided callback gets called when
     * initial/default settings are available. The callback may be called after a delay, e.g. after the settings have
     * been retrieved from a server, or immediately, e.g. when the implementation does not query information from
     * server. This method produces no side-effects if it gets called multiple times. After this method has been called
     * for the first time, subsequent calls will not produce any server round-trips or LocalStorage accesses due to its
     * internal caching.
     * 
     * @param settingsReceiverCallback
     *            The callback which supplies the caller with initial settings
     */
    @Override
    public void getInitialSettings(final OnSettingsLoadedCallback<S> callback) {
        final S systemDefaultSettings = rootLifecycle.createDefaultSettings();
        if (cachedSettingsRepresentation == null) {
            if (settingsStorageManager != null) {
                settingsStorageManager.retrieveSettingsRepresentation(
                        new OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings>() {

                            @Override
                            public void onError(Throwable caught,
                                    StorableRepresentationOfDocumentAndUserSettings fallbackSettingsRepresentation) {
                                callback.onError(caught,
                                        getSettingsObject(fallbackSettingsRepresentation, systemDefaultSettings));
                            }

                            @Override
                            public void onSuccess(
                                    StorableRepresentationOfDocumentAndUserSettings settingsRepresentation) {
                                callback.onSuccess(getSettingsObject(settingsRepresentation, systemDefaultSettings));
                            }

                        });
            } else {
                callback.onSuccess(getSettingsObject(new StorableRepresentationOfDocumentAndUserSettings(null, null),
                        systemDefaultSettings));
            }

        } else {
            S newDefaultSettings = settingsBuildingPipeline.getSettingsObject(systemDefaultSettings,
                    cachedSettingsRepresentation);
            callback.onSuccess(newDefaultSettings);
        }
    }

    private S getSettingsObject(StorableRepresentationOfDocumentAndUserSettings settingsRepresentation,
            S systemDefaultSettings) {
        cachedSettingsRepresentation = settingsRepresentation;
        S settingsObject = settingsBuildingPipeline.getSettingsObject(systemDefaultSettings,
                cachedSettingsRepresentation);
        return settingsObject;
    }

    /**
     * Checks whether the {@link Settings} of the provided {@link Component} are storable and whether the underlying
     * implementation enables {@link #makeSettingsDefault(Component, Settings)} calls for it.
     * 
     * @param component
     *            The component with potentially storable settings
     * @return {@code true} if the settings of the component are storable <b>AND</b> the {@link ComponentContext}
     *         implementation has settings storage support
     */
    @Override
    public boolean isStorageSupported(Component<?> component) {
        if (!component.hasSettings()) {
            return false;
        }
        if (settingsStorageManager == null) {
            return false;
        }
        Settings settings = component.getSettings();
        if (settings instanceof GenericSerializableSettings) {
            return true;
        }
        return false;
    }

    /**
     * Stores the provided {@link Settings} of the provided {@link Component} as User Settings which are context-independent. Make sure to
     * call this method only when {@link #isStorageSupported(Component)} method returns {@code true} for the provided
     * {@link Component}.
     * 
     * @param component
     *            The component which corresponds to the provided {@link Settings}
     * @param newDefaultSettings
     *            The {@link Settings} to be stored
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    @Override
    public <CS extends Settings> void makeSettingsDefault(Component<CS> component, CS newDefaultSettings,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
        Iterable<String> path = component.getPath();
        ComponentLifecycle<CS> targetLifecycle = ComponentUtils.determineLifecycle(path,
                rootLifecycle);
        CS userSettings = targetLifecycle.extractUserSettings(newDefaultSettings);
        updateUserSettings(path, userSettings, targetLifecycle.createDefaultSettings(), onSettingsStoredCallback);
    }

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
    @Override
    public <CS extends Settings> void storeSettingsForContext(Component<CS> component, CS newSettings,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        Iterable<String> path = component.getPath();
        ComponentLifecycle<CS> targetLifecycle = ComponentUtils.determineLifecycle(path,
                rootLifecycle);
        CS documentSettings = targetLifecycle.extractDocumentSettings(newSettings);
        updateDocumentSettings(component.getPath(), documentSettings, targetLifecycle.createDefaultSettings(),
                onSettingsStoredCallback);
    }

    @Override
    public <CS extends Settings> void resetSettingsToDefault(Component<CS> component, CS newSettings,
            OnSettingsLoadedCallback<CS> onSettingsStoredCallback) {
        Iterable<String> path = component.getPath();
        ComponentLifecycle<CS> targetLifecycle = ComponentUtils.determineLifecycle(path,
                rootLifecycle);
        removeGlobalAndContextSettings(path, targetLifecycle, new OnSettingsStoredCallback() {
            
            @Override
            public void onSuccess() {
                getInitialSettingsForComponent(component, onSettingsStoredCallback);
            }
            
            @Override
            public void onError(Throwable caught) {
                onSettingsStoredCallback.onError(caught, targetLifecycle.createDefaultSettings());
            }
        });
       
    }

    private void removeGlobalAndContextSettings(Iterable<String> path, ComponentLifecycle<?> targetLifecycle,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        settingsStorageManager.retrieveSettingsRepresentation(
                new OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings>() {

                    @Override
                    public void onError(Throwable caught,
                            StorableRepresentationOfDocumentAndUserSettings fallbackDefaultSettings) {
                        onSettingsStoredCallback.onError(caught);
                    }

                    @Override
                    public void onSuccess(StorableRepresentationOfDocumentAndUserSettings settingsRepresentation) {
                        StorableSettingsRepresentation documentSettingsRepresentationRoot = StorableSettingsRepresentation
                                .patchSettingsRepresentation(settingsRepresentation.getDocumentSettingsRepresentation(),
                                        path, null);

                        StorableSettingsRepresentation userSettingsRepresentationRoot = StorableSettingsRepresentation
                                .patchSettingsRepresentation(settingsRepresentation.getUserSettingsRepresentation(),
                                        path, null);

                        StorableRepresentationOfDocumentAndUserSettings settingsRepresentationToStore = new StorableRepresentationOfDocumentAndUserSettings(
                                userSettingsRepresentationRoot, documentSettingsRepresentationRoot);
                        cachedSettingsRepresentation = settingsRepresentationToStore;
                        settingsStorageManager.storeSettingsRepresentations(settingsRepresentationToStore,
                                onSettingsStoredCallback);
                    }
                });
    }

    /**
     * Updates the User Settings' tree with the new settings provided. The settings node with the provided path is going
     * to be created/replaced with the new settings. This method may produce a server round-trip if the user is logged
     * in.
     * 
     * @param path
     *            The path of the settings' node to create/update
     * @param newUserSettings
     *            User Settings which corresponds to the provided path
     * @param newInstance
     *            A fresh dummy instance of the settings type which will be used as temporary helper (defaultValues and
     *            values of the instance are completely ignored)
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    private<CS extends Settings> void updateUserSettings(final Iterable<String> path, final Settings newUserSettings, final CS newInstance,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
        settingsStorageManager.retrieveSettingsRepresentation(
                new OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings>() {

                    @Override
                    public void onError(Throwable caught,
                            StorableRepresentationOfDocumentAndUserSettings fallbackDefaultSettings) {
                        onSettingsStoredCallback.onError(caught);
                    }

                    @Override
                    public void onSuccess(StorableRepresentationOfDocumentAndUserSettings settingsRepresentation) {
                        StorableSettingsRepresentation newUserSettingsRepresentationOfComponent = settingsBuildingPipeline.getStorableRepresentationOfUserSettings(newUserSettings, newInstance, path);
                        StorableSettingsRepresentation userSettingsRepresentationRoot = StorableSettingsRepresentation.patchSettingsRepresentation(
                                settingsRepresentation.getUserSettingsRepresentation(), path,
                                newUserSettingsRepresentationOfComponent);
                        
                        cachedSettingsRepresentation = new StorableRepresentationOfDocumentAndUserSettings(
                                userSettingsRepresentationRoot,
                                cachedSettingsRepresentation.getDocumentSettingsRepresentation());
                        
                        StorableRepresentationOfDocumentAndUserSettings settingsRepresentationToStore = new StorableRepresentationOfDocumentAndUserSettings(userSettingsRepresentationRoot,
                                null);
                        settingsStorageManager.storeSettingsRepresentations(settingsRepresentationToStore,
                                onSettingsStoredCallback);
                    }

                });
    }

    /**
     * Updates the Document Settings' tree with the new settings provided. The settings node with the provided path is
     * going to be created/replaced with the new settings. This method may produce a server round-trip if the user is
     * logged in.
     * 
     * @param path
     *            The path of the settings' node to create/update
     * @param newDocumentSettings
     *            Document Settings which corresponds to the provided path
     * @param newInstance
     *            A fresh dummy instance of the settings type which will be used as temporary helper (defaultValues and
     *            values of the instance are completely ignored)
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    private<CS extends Settings> void updateDocumentSettings(final Iterable<String> path, final CS newDocumentSettings, final CS newInstance,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
        settingsStorageManager.retrieveSettingsRepresentation(
                new OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings>() {

                    @Override
                    public void onError(Throwable caught,
                            StorableRepresentationOfDocumentAndUserSettings fallbackDefaultSettings) {
                        onSettingsStoredCallback.onError(caught);
                    }

                    @Override
                    public void onSuccess(StorableRepresentationOfDocumentAndUserSettings settingsRepresentation) {
                        StorableSettingsRepresentation newDocumentSettingsRepresentationOfComponent = settingsBuildingPipeline.getStorableRepresentationOfDocumentSettings(newDocumentSettings, newInstance, cachedSettingsRepresentation, path);
                        StorableSettingsRepresentation documentSettingsRepresentationRoot = StorableSettingsRepresentation.patchSettingsRepresentation(
                                settingsRepresentation.getDocumentSettingsRepresentation(), path,
                                newDocumentSettingsRepresentationOfComponent);
                        
                        cachedSettingsRepresentation = new StorableRepresentationOfDocumentAndUserSettings(
                                cachedSettingsRepresentation.getUserSettingsRepresentation(),
                                documentSettingsRepresentationRoot);
                        
                        StorableRepresentationOfDocumentAndUserSettings settingsRepresentationToStore = new StorableRepresentationOfDocumentAndUserSettings(null,
                                documentSettingsRepresentationRoot);
                        settingsStorageManager.storeSettingsRepresentations(settingsRepresentationToStore,
                                onSettingsStoredCallback);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        if (settingsStorageManager != null) {
            settingsStorageManager.dispose();
        }
    }
    
}
