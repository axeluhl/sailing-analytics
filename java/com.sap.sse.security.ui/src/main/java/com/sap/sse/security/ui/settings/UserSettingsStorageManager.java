package com.sap.sse.security.ui.settings;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.CallbacksJoinerHelper;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * The settings are stored in HTML5 LocalStorage, as well as on server in the user's account, in case the user is
 * logged.
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 * @see SettingsStorageManager
 */
public class UserSettingsStorageManager<S extends Settings> extends SimpleSettingsStorageManager<S> {

    /**
     * The key which is associated with the global settings. Different keys will cause multiple/different settings
     * instances stored in the storage.
     */
    private final String storageGlobalKey;

    /**
     * The key which is used to store the context specific settings. Each context with own context specific settings
     * must have a unique key.
     */
    private final String storageContextSpecificKey;

    private Throwable lastError = null;

    private UserService userService;

    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();

    /**
     * This is used, to ensure that only once the data is loaded remote, if a user logs in later, he must refresh, to
     * avoid "complicated problems"
     */
    private boolean initialUserSetting = false;

    /**
     * 
     * @param userService
     *            The {@link UserService} which is used for settings storage on server when the current user is logged
     *            in
     * @param globalDefinitionId
     *            The key which is associated with the global settings. Different keys will cause multiple/different
     *            settings instances stored in the storage.
     * @param contextDefinitionId
     *            The key which is used to store the context specific settings. Each context with own context specific
     *            settings must have a unique key.
     */
    public UserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId) {
        this.userService = userService;
        this.storageGlobalKey = storageDefinitionId.generateStorageGlobalKey();
        this.storageContextSpecificKey = storageDefinitionId.generateStorageContextSpecificKey();
    }
    
    @Override
    public boolean supportsStore() {
        return true;
    }

    private void storeContextSpecificSettingsJsonOnLocalStorage(String serializedContextSpecificSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageContextSpecificKey);
            localStorage.setItem(storageContextSpecificKey, serializedContextSpecificSettings);
        }
    }

    private void storeContextSpecificSettingsJsonOnServer(String serializedContextSpecificSettings) {
        if (userService.getCurrentUser() != null) {
            userService.setPreference(storageContextSpecificKey, serializedContextSpecificSettings);
        }
    }

    private void storeGlobalSettingsJsonOnLocalStorage(String serializedGlobalSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageGlobalKey);
            localStorage.setItem(storageGlobalKey, serializedGlobalSettings);
        }
    }

    private void storeGlobalSettingsJsonOnServer(String serializedGlobalSettings) {
        if (userService.getCurrentUser() != null) {
            userService.setPreference(storageGlobalKey, serializedGlobalSettings);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void retrieveDefaultSettings(S defaultSettings, final OnSettingsLoadedCallback<S> asyncCallback) {
        final SettingsJsonRetrievement settingsJsonRetrievement = new SettingsJsonRetrievement(defaultSettings);
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {

            @Override
            public void onUserStatusChange(UserDTO user) {
                if (!initialUserSetting) {
                    initialUserSetting = true;
                    if (user != null) {
                        AsyncCallback<String> globalSettingsAsyncCallback = new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                lastError = caught;
                                settingsJsonRetrievement.receiveError(caught);
                                onSuccess(null);
                            }

                            @Override
                            public void onSuccess(String globalSettingsJson) {
                                settingsJsonRetrievement.receiveGlobalSettingsJson(globalSettingsJson);
                                if (settingsJsonRetrievement.hasAllCallbacksReceived()) {
                                    continueRetrieveDefaultSettings(settingsJsonRetrievement, asyncCallback);
                                }
                            }
                        };
                        retrieveGlobalSettingsJsonFromServerRaw(globalSettingsAsyncCallback);

                        AsyncCallback<String> contextSpecificSettingsAsyncCallback = new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                lastError = caught;
                                settingsJsonRetrievement.receiveError(caught);
                                onSuccess(null);
                            }

                            @Override
                            public void onSuccess(String contextSpecificSettingsJson) {
                                settingsJsonRetrievement
                                        .receiveContextSpecificSettingsJson(contextSpecificSettingsJson);
                                if (settingsJsonRetrievement.hasAllCallbacksReceived()) {
                                    continueRetrieveDefaultSettings(settingsJsonRetrievement, asyncCallback);
                                }
                            }
                        };
                        retrieveContextSpecificSettingsJsonFromServerRaw(contextSpecificSettingsAsyncCallback);
                    } else {
                        settingsJsonRetrievement.receiveContextSpecificSettingsJson(null);
                        settingsJsonRetrievement.receiveGlobalSettingsJson(null);
                        continueRetrieveDefaultSettings(settingsJsonRetrievement, asyncCallback);
                    }
                }
            }
        }, true);
    }

    private void continueRetrieveDefaultSettings(
            UserSettingsStorageManager<S>.SettingsJsonRetrievement settingsJsonRetrievement,
            OnSettingsLoadedCallback<S> callback) {
        S defaultSettings = settingsJsonRetrievement.getDefaultSettings();

        // has been any global settings from server retrieved? yes => apply as default and override LocalStorage
        if (settingsJsonRetrievement.getGlobalSettingsJson() != null) {
            defaultSettings = deserializeFromJson(defaultSettings, settingsJsonRetrievement.getGlobalSettingsJson());
            storeGlobalSettingsJsonOnLocalStorage(settingsJsonRetrievement.getGlobalSettingsJson());
        } else {
            String localStorageGlobalSettingsJson = retrieveGlobalSettingsJsonFromLocalStorage();
            if (localStorageGlobalSettingsJson != null) {
                // server has no global settings, local storage has => apply local storage settings and store them on
                // server
                defaultSettings = deserializeFromJson(defaultSettings, localStorageGlobalSettingsJson);
                storeGlobalSettingsJsonOnServer(localStorageGlobalSettingsJson);
            }
        }

        // has been any context specific settings from server retrieved? yes => apply as default and override
        // LocalStorage
        if (settingsJsonRetrievement.getContextSpecificSettingsJson() != null) {
            defaultSettings = deserializeFromJson(defaultSettings,
                    settingsJsonRetrievement.getContextSpecificSettingsJson());
            storeContextSpecificSettingsJsonOnLocalStorage(settingsJsonRetrievement.getContextSpecificSettingsJson());
        } else {
            String localStorageContextSpecificSettingsJson = retrieveContextSpecificSettingsJsonFromLocalStorage();
            if (localStorageContextSpecificSettingsJson != null) {
                // server has no context specific settings, local storage has => apply local storage settings and store
                // them on server
                defaultSettings = deserializeFromJson(defaultSettings, localStorageContextSpecificSettingsJson);
                storeContextSpecificSettingsJsonOnServer(localStorageContextSpecificSettingsJson);
            }
        }

        // URL settings have highest precedence
        defaultSettings = retrieveDefaultSettingsFromUrl(defaultSettings);

        if (settingsJsonRetrievement.isErrorOccurred()) {
            callback.onError(settingsJsonRetrievement.getCaught(), defaultSettings);
        } else {
            callback.onSuccess(defaultSettings);
        }
    }

    @SuppressWarnings("unchecked")
    private S deserializeFromJson(S defaultSettings, String jsonToDeserialize) {
        if (defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) jsonSerializer.deserialize((GenericSerializableSettings) defaultSettings,
                    jsonToDeserialize);
        } else if (defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) jsonSerializer.deserialize((SettingsMap) defaultSettings, jsonToDeserialize);
        }
        return defaultSettings;
    }

    private String retrieveGlobalSettingsJsonFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            return localStorage.getItem(storageGlobalKey);
        }
        return null;
    }

    public String retrieveContextSpecificSettingsJsonFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            return localStorage.getItem(storageContextSpecificKey);
        }
        return null;
    }

    protected void retrieveGlobalSettingsJsonFromServerRaw(AsyncCallback<String> globalSettingsAsyncCallback) {
        userService.getPreference(storageGlobalKey, globalSettingsAsyncCallback);
    }

    public void retrieveContextSpecificSettingsJsonFromServerRaw(AsyncCallback<String> asyncCallback) {
        userService.getPreference(storageContextSpecificKey, asyncCallback);
    }

    private S retrieveDefaultSettingsFromUrl(S defaultSettings) {
        return deserializeFromCurrentUrl(defaultSettings);
    }

    /**
     * Helper class to leverage two parallel server calls in order to retrieve global and context specific settings
     * without additional unnecessary server-roundtrips and provide the results when both call results have been
     * received.
     * 
     * @see CallbacksJoinerHelper
     *
     */
    private class SettingsJsonRetrievement extends CallbacksJoinerHelper<String, String> {
        private S defaultSettings;

        public SettingsJsonRetrievement(S defaultSettings) {
            this.defaultSettings = defaultSettings;
        }

        public String getGlobalSettingsJson() {
            return getFirstCallbackResult();
        }

        public void receiveGlobalSettingsJson(String globalSettingsJson) {
            receiveFirstCallbackResult(globalSettingsJson);
        }

        public String getContextSpecificSettingsJson() {
            return getSecondCallbackResult();
        }

        public void receiveContextSpecificSettingsJson(String contextSpecificSettingsJson) {
            receiveSecondCallbackResult(contextSpecificSettingsJson);
        }

        public S getDefaultSettings() {
            return defaultSettings;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Throwable getLastError() {
        return lastError;
    }

    public void retrieveGlobalSettingsJsonFromServer(final AsyncCallback<JSONObject> asyncCallback) {
        retrieveGlobalSettingsJsonFromServerRaw(new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (result == null) {
                    asyncCallback.onSuccess(null);
                } else {
                    asyncCallback.onSuccess(jsonSerializer.parseStringToJsonObject(result));
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                asyncCallback.onFailure(caught);
            }
        });
    }

    public void retrieveContextSpecificSettingsJsonFromServer(final AsyncCallback<JSONObject> asyncCallback) {
        retrieveContextSpecificSettingsJsonFromServerRaw(new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (result == null) {
                    asyncCallback.onSuccess(null);
                } else {
                    asyncCallback.onSuccess(jsonSerializer.parseStringToJsonObject(result));
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                asyncCallback.onFailure(caught);
            }
        });
    }

    @Override
    public JSONValue settingsToJSON(Settings newSettings) {
        if (newSettings instanceof GenericSerializableSettings) {
            return jsonSerializer.serialize((GenericSerializableSettings) newSettings);
        }
        throw new IllegalStateException("Requested save of settings that is not Serializable!");
    }

    @Override
    public void storeGlobalSettings(JSONObject patchedGlobal) {
        String tostore = jsonSerializer.jsonObjectToString(patchedGlobal);
        storeGlobalSettingsJsonOnServer(tostore);
        storeGlobalSettingsJsonOnLocalStorage(tostore);
    }

    @Override
    public void storeContextSpecificSettings(JSONObject contextSpecificSettings) {
        String tostore = jsonSerializer.jsonObjectToString(contextSpecificSettings);
        storeContextSpecificSettingsJsonOnServer(tostore);
        storeContextSpecificSettingsJsonOnLocalStorage(tostore);
    }

    @Override
    public void retrieveGlobalSettingsJson(AsyncCallback<JSONObject> asyncCallback) {
        if (userService.getCurrentUser() == null) {
            String raw = retrieveContextSpecificSettingsJsonFromLocalStorage();
            if (raw == null) {
                asyncCallback.onSuccess(null);
            } else {
                asyncCallback.onSuccess(jsonSerializer.parseStringToJsonObject(raw));
            }
        } else {
            retrieveContextSpecificSettingsJsonFromServer(asyncCallback);
        }
    }

    @Override
    public void retrieveContextSpecificSettingsJson(AsyncCallback<JSONObject> asyncCallback) {
        if (userService.getCurrentUser() == null) {
            String raw = retrieveGlobalSettingsJsonFromLocalStorage();
            if (raw == null) {
                asyncCallback.onSuccess(null);
            } else {
                asyncCallback.onSuccess(jsonSerializer.parseStringToJsonObject(raw));
            }
        } else {
            retrieveGlobalSettingsJsonFromServer(asyncCallback);
        }
    }

    /**
     * Creates a {@link SettingsStorageManager} instance based on the ignoreLocalSettings URL flag. if
     * ignoreLocalSettings is set to <code>true</code>, a SimpleSettingsStorageManager is created. A
     * UserSettingsStorageManager is created otherwise.
     */
    public static <S extends Settings> SettingsStorageManager<S> createSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId) {
        if (getIgnoreLocalSettings().isIgnoreLocalSettings()) {
            return new SimpleSettingsStorageManager<>();
        }
        return new UserSettingsStorageManager<>(userService, storageDefinitionId);
    }
    
    public static IgnoreLocalSettings getIgnoreLocalSettings() {
        return new SettingsToUrlSerializer().deserializeFromCurrentLocation(new IgnoreLocalSettings());
    }
}
