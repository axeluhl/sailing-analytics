package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.common.settings.util.SettingsMergeUtils;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.shared.settings.ComponentUtils;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.PersistableSettingsRepresentations;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.SettingsSerializationHelper;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;
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
     * Persistable settings representations which have been already queried from persistence layer, or {@code null} if
     * previous settings retrievement has not been initiated.
     */
    protected PersistableSettingsRepresentations<JSONObject> retrievedPersistableSettingsRepresentations = null;

    /**
     * Manages the persistence layer of settings.
     */
    protected final SettingsStorageManager<String> settingsStorageManager;

    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinition
     *            The definition for User Settings and Document Settings storage keys
     */
    public ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StorageDefinition storageDefinition) {
        this(rootLifecycle, userService, storageDefinition, new SettingsSerializationHelper());
    }

    protected ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StorageDefinition storageDefinition, SettingsSerializationHelper settingsSerializationHelper) {
        this(rootLifecycle, userService, storageDefinition, settingsSerializationHelper,
                new UserSettingsBuildingPipeline(settingsSerializationHelper));
    }

    protected ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StorageDefinition storageDefinition, SettingsSerializationHelper settingsSerializationHelper,
            SettingsBuildingPipeline<JSONObject> settingsBuildingPipeline) {
        super(rootLifecycle, settingsSerializationHelper, settingsBuildingPipeline);
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
        if (retrievedPersistableSettingsRepresentations == null) {
            if (settingsStorageManager != null) {
                settingsStorageManager.retrieveSettingsRepresentation(
                        new OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>>() {

                            @Override
                            public void onError(Throwable caught,
                                    PersistableSettingsRepresentations<String> fallbackSettingsRepresentation) {
                                callback.onError(caught,
                                        getSettingsObject(fallbackSettingsRepresentation, systemDefaultSettings));
                            }

                            @Override
                            public void onSuccess(PersistableSettingsRepresentations<String> settingsRepresentation) {
                                callback.onSuccess(getSettingsObject(settingsRepresentation, systemDefaultSettings));
                            }

                        });
            } else {
                callback.onSuccess(getSettingsObject(new PersistableSettingsRepresentations<String>(null, null),
                        systemDefaultSettings));
            }

        } else {
            S newDefaultSettings = settingsBuildingPipeline.getSettingsObject(systemDefaultSettings,
                    retrievedPersistableSettingsRepresentations);
            callback.onSuccess(newDefaultSettings);
        }
    }

    private S getSettingsObject(PersistableSettingsRepresentations<String> settingsRepresentation,
            S systemDefaultSettings) {
        retrievedPersistableSettingsRepresentations = convertToSettingsJson(settingsRepresentation);
        S settingsObject = settingsBuildingPipeline.getSettingsObject(systemDefaultSettings,
                retrievedPersistableSettingsRepresentations);
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
        if (settings instanceof SettingsMap || settings instanceof GenericSerializableSettings) {
            return true;
        }
        return false;
    }

    /**
     * Stores the provided {@link Settings} of the provided {@link Component} as "User Settings" globally. Make sure to
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
        ComponentLifecycle<CS> targetLifecycle = ComponentUtils.determineLifecycle(new ArrayList<>(component.getPath()),
                rootLifecycle);
        CS globalSettings = targetLifecycle.extractGlobalSettings(newDefaultSettings);
        updateGlobalSettings(component.getPath(), globalSettings, onSettingsStoredCallback);
    }

    /**
     * Stores the provided {@link Settings} of the provided {@link Component} as "Document Settings" (old term "Context
     * specific Settings") for the current context. Make sure to call this method only when
     * {@link #isStorageSupported(Component)} method returns {@code true} for the provided {@link Component}.
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
        ComponentLifecycle<CS> targetLifecycle = ComponentUtils.determineLifecycle(new ArrayList<>(component.getPath()),
                rootLifecycle);
        CS contextSpecificSettings = targetLifecycle.extractContextSpecificSettings(newSettings);
        updateContextSpecificSettings(component.getPath(), contextSpecificSettings, onSettingsStoredCallback);
    }

    /**
     * Patches the settings tree with the new settings provided. The settings node with the provided path is going to be
     * created/replaced with the new settings.
     * 
     * @param root
     *            The root node of the settings tree
     * @param path
     *            The path of the node to create/update
     * @param newSettings
     *            The new settings with that the target node in the settings tree is going to be updated
     * @param pipelineLevel
     *            The scope of settings used for settings storage (e.g. User Settings, or Document Settings)
     * @return The patched settings located by provided path as JSON
     */
    private JSONObject patchJsonObject(JSONObject root, List<String> path, Settings newSettings,
            PipelineLevel pipelineLevel) {
        newSettings = cloneStorableSettings(newSettings, path);
        return patchJsonObject(root, path, Collections.unmodifiableList(new ArrayList<>(path)), newSettings,
                pipelineLevel);
    }

    @SuppressWarnings("unchecked")
    private <CS extends Settings> CS cloneStorableSettings(CS settingsToClone, List<String> path) {
        CS clonedSettings = ComponentUtils.determineComponentSettingsFromPerspectiveSettings(new ArrayList<>(path),
                rootLifecycle.createDefaultSettings());
        if (clonedSettings instanceof GenericSerializableSettings) {
            GenericSerializableSettings newSettings = (GenericSerializableSettings) clonedSettings;
            GenericSerializableSettings originalSettings = (GenericSerializableSettings) settingsToClone;
            SettingsDefaultValuesUtils.keepDefaults(originalSettings, newSettings);
            SettingsMergeUtils.setValues(originalSettings, newSettings);
            clonedSettings = (CS) newSettings;
        }
        // TODO maybe add support for SettingsMap? Currently, SettingsMap is never provided as parameter to this method
        return clonedSettings;
    }

    /**
     * Internal helper method for {@link #patchJsonObject(JSONObject, List, Settings, PipelineLevel)}
     * 
     * @param root
     * @param path
     * @param originalPath
     * @param newSettings
     * @param pipelineLevel
     * @return
     */
    private JSONObject patchJsonObject(JSONObject root, List<String> path, List<String> originalPath,
            Settings newSettings, PipelineLevel pipelineLevel) {
        if (path.isEmpty()) {
            return settingsBuildingPipeline.getPersistableSettingsRepresentation(newSettings, pipelineLevel,
                    originalPath);
        }
        if (root == null) {
            root = new JSONObject();
        }
        String current = path.remove(path.size() - 1);
        // we need to go further
        if (!path.isEmpty()) {
            JSONValue child = root.get(current);
            boolean haskey = root.containsKey(current);
            if (child == null || child.isObject() == null) {
                if (haskey) {
                    GWT.log("Warning: replacing some subtree element that is wrong type!");
                }
                child = new JSONObject();
                root.put(current, child);
            }
            return patchJsonObject(child.isObject(), path, originalPath, newSettings, pipelineLevel);
        } else {
            JSONObject json = settingsBuildingPipeline.getPersistableSettingsRepresentation(newSettings, pipelineLevel,
                    originalPath);
            root.put(current, json);
        }
        return root;
    }

    /**
     * Updates the User Settings' tree with the new settings provided. The settings node with the provided path is going
     * to be created/replaced with the new settings. This method may produce a server round-trip if the user is logged
     * in.
     * 
     * @param path
     *            The path of the settings' node to create/update
     * @param globalSettings
     *            User Settings which corresponds to the provided path
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    private void updateGlobalSettings(final ArrayList<String> path, final Settings globalSettings,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
        settingsStorageManager.retrieveSettingsRepresentation(
                new OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>>() {

                    @Override
                    public void onError(Throwable caught,
                            PersistableSettingsRepresentations<String> fallbackDefaultSettings) {
                        onSettingsStoredCallback.onError(caught);
                    }

                    @Override
                    public void onSuccess(PersistableSettingsRepresentations<String> settingsRepresentation) {
                        PersistableSettingsRepresentations<JSONObject> result = convertToSettingsJson(
                                settingsRepresentation);
                        final JSONObject patchedGlobalSettings = patchJsonObject(
                                result.getGlobalSettingsRepresentation(), new ArrayList<>(path), globalSettings,
                                PipelineLevel.GLOBAL_DEFAULTS);
                        settingsRepresentation = convertToSettingsStrings(
                                new PersistableSettingsRepresentations<JSONObject>(patchedGlobalSettings, null));
                        retrievedPersistableSettingsRepresentations = new PersistableSettingsRepresentations<JSONObject>(
                                patchedGlobalSettings,
                                retrievedPersistableSettingsRepresentations.getContextSpecificSettingsRepresentation());
                        settingsStorageManager.storeSettingsRepresentation(settingsRepresentation,
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
     * @param contextSpecificSettings
     *            Document Settings which corresponds to the provided path
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process finishes
     */
    private void updateContextSpecificSettings(final ArrayList<String> path, final Settings contextSpecificSettings,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
        settingsStorageManager.retrieveSettingsRepresentation(
                new OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>>() {

                    @Override
                    public void onError(Throwable caught,
                            PersistableSettingsRepresentations<String> fallbackDefaultSettings) {
                        onSettingsStoredCallback.onError(caught);
                    }

                    @Override
                    public void onSuccess(PersistableSettingsRepresentations<String> settingsRepresentation) {
                        PersistableSettingsRepresentations<JSONObject> result = convertToSettingsJson(
                                settingsRepresentation);
                        final JSONObject patchedContextSpecificSettings = patchJsonObject(
                                result.getContextSpecificSettingsRepresentation(), new ArrayList<>(path),
                                contextSpecificSettings, PipelineLevel.CONTEXT_SPECIFIC_DEFAULTS);
                        settingsRepresentation = convertToSettingsStrings(
                                new PersistableSettingsRepresentations<JSONObject>(null,
                                        patchedContextSpecificSettings));
                        retrievedPersistableSettingsRepresentations = new PersistableSettingsRepresentations<JSONObject>(
                                retrievedPersistableSettingsRepresentations.getGlobalSettingsRepresentation(),
                                patchedContextSpecificSettings);
                        settingsStorageManager.storeSettingsRepresentation(settingsRepresentation,
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

    public PersistableSettingsRepresentations<JSONObject> convertToSettingsJson(
            PersistableSettingsRepresentations<String> settingsStrings) {
        JSONObject globalSettingsJson = settingsSerializationHelper
                .convertStringToJson(settingsStrings.getGlobalSettingsRepresentation());
        JSONObject contextSpecificSettingsJson = settingsSerializationHelper
                .convertStringToJson(settingsStrings.getContextSpecificSettingsRepresentation());
        return new PersistableSettingsRepresentations<JSONObject>(globalSettingsJson, contextSpecificSettingsJson);
    }

    public PersistableSettingsRepresentations<String> convertToSettingsStrings(
            PersistableSettingsRepresentations<JSONObject> settingsJsons) {
        String globalSettingsString = settingsSerializationHelper
                .convertJsonToString(settingsJsons.getGlobalSettingsRepresentation());
        String contextSpecificSettingsString = settingsSerializationHelper
                .convertJsonToString(settingsJsons.getContextSpecificSettingsRepresentation());
        return new PersistableSettingsRepresentations<String>(globalSettingsString, contextSpecificSettingsString);
    }

}
