package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.client.shared.perspective.SettingsStrings;
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

    private UserService userService;

    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();

    /**
     * This is used, to ensure that only once the data is loaded remote, if a user logs in later, he must refresh, to
     * avoid "complicated problems"
     */
    private boolean initialUserSetting = false;
    
    private UserStatusEventHandler userStatusEventHandler = null;

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

    private void storeSettingsStringsOnLocalStorage(SettingsStrings settingsStrings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageContextSpecificKey);
            if(settingsStrings.getGlobalSettingsString() != null) {
                localStorage.setItem(storageGlobalKey, settingsStrings.getGlobalSettingsString());
            }
            if(settingsStrings.getContextSpecificSettingsString() != null) {
                localStorage.setItem(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString());
            }
        }
    }

    private void storeSettingsStringsOnServer(SettingsStrings settingsStrings, final OnSettingsStoredCallback onSettingsStoredCallback) {
        Map<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put(storageGlobalKey, settingsStrings.getGlobalSettingsString());
        keyValuePairs.put(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString());
        userService.setPreferences(keyValuePairs, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                onSettingsStoredCallback.onError(caught);
            }

            @Override
            public void onSuccess(Void result) {
                onSettingsStoredCallback.onSuccess();
            }
        });
    }

    public void retrieveDefaultSettings(S defaultSettings, final OnSettingsLoadedCallback<S> callback) {
        if(userStatusEventHandler != null) {
            throw new IllegalStateException("The contract between ComponentContext and SettingsStorageManager enforces this method to be called only once");
        }
        this.userStatusEventHandler = new UserStatusEventHandler() {

            @Override
            public void onUserStatusChange(UserDTO user) {
                //call this method always on user status change event in order to cause sync between local storage and server
                retrieveSettingsJson(new AsyncCallback<SettingsJsons>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        if(!initialUserSetting) {
                            initialUserSetting = true;
                            SettingsStrings localStorageSettings = retrieveSettingsStringsFromLocalStorage();
                            S newDefaultSettings = convertToSettings(defaultSettings, localStorageSettings);
                            callback.onError(caught, newDefaultSettings);
                        }
                    }

                    @Override
                    public void onSuccess(SettingsJsons settingsJsons) {
                        if(!initialUserSetting) {
                            initialUserSetting = true;
                            callback.onSuccess(convertToSettings(defaultSettings, settingsJsons));
                        }
                    }
                });
            }

        };
        userService.addUserStatusEventHandler(userStatusEventHandler, true);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if(userStatusEventHandler != null) {
            userService.removeUserStatusEventHandler(userStatusEventHandler);
        }
    }
    
    private SettingsStrings retrieveSettingsStringsFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            return new SettingsStrings(localStorage.getItem(storageGlobalKey), localStorage.getItem(storageContextSpecificKey));
        }
        return new SettingsStrings(null, null);
    }
    
    private SettingsJsons convertToSettingsJson(SettingsStrings settingsStrings) {
        JSONObject globalSettingsJson = settingsStrings.getGlobalSettingsString() == null ? null : jsonSerializer.parseStringToJsonObject(settingsStrings.getGlobalSettingsString());
        JSONObject contextSpecificSettingsJson = settingsStrings.getContextSpecificSettingsString() == null ? null : jsonSerializer.parseStringToJsonObject(settingsStrings.getContextSpecificSettingsString());
        return new SettingsJsons(globalSettingsJson, contextSpecificSettingsJson);
    }
    
    private SettingsStrings convertToSettingsStrings(SettingsJsons settingsJsons) {
        String globalSettingsString = settingsJsons.getGlobalSettingsJson() == null ? null : jsonSerializer.jsonObjectToString(settingsJsons.getGlobalSettingsJson());
        String contextSpecificSettingsString = settingsJsons.getContextSpecificSettingsJson() == null ? null : jsonSerializer.jsonObjectToString(settingsJsons.getContextSpecificSettingsJson());
        return new SettingsStrings(globalSettingsString, contextSpecificSettingsString);
    }
    
    private S convertToSettings(S defaultSettings, SettingsJsons settingsJsons) {
        defaultSettings = deserializeFromJson(defaultSettings, settingsJsons.getGlobalSettingsJson());
        defaultSettings = deserializeFromJson(defaultSettings, settingsJsons.getContextSpecificSettingsJson());
        defaultSettings = retrieveDefaultSettingsFromUrl(defaultSettings);
        return defaultSettings;
    }
    
    private S convertToSettings(S defaultSettings, SettingsStrings settingsStrings) {
        SettingsJsons settingsJsons = convertToSettingsJson(settingsStrings);
        return convertToSettings(defaultSettings, settingsJsons);
    }
    
    @SuppressWarnings("unchecked")
    private S deserializeFromJson(S defaultSettings, JSONObject jsonToDeserialize) {
        if (defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) jsonSerializer.deserialize((GenericSerializableSettings) defaultSettings,
                    jsonToDeserialize);
        } else if (defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) jsonSerializer.deserialize((SettingsMap) defaultSettings, jsonToDeserialize);
        }
        return defaultSettings;
    }

    private S retrieveDefaultSettingsFromUrl(S defaultSettings) {
        return deserializeFromCurrentUrl(defaultSettings);
    }

    private void retrieveSettingsStringsFromServer(final AsyncCallback<SettingsStrings> asyncCallback) {
        List<String> keys = new ArrayList<>(2);
        keys.add(storageGlobalKey);
        keys.add(storageContextSpecificKey);
        userService.getPreferences(keys, new AsyncCallback<Map<String,String>>() {
            
            @Override
            public void onSuccess(Map<String, String> result) {
                SettingsStrings settingsStrings = new SettingsStrings(result.get(storageGlobalKey), result.get(storageContextSpecificKey));
                asyncCallback.onSuccess(settingsStrings);
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
    public void storeSettingsJsons(SettingsJsons settingsJsons, OnSettingsStoredCallback onSettingsStoredCallback) {
        SettingsStrings settingsStrings = convertToSettingsStrings(settingsJsons);
        storeSettingsStringsOnLocalStorage(settingsStrings);
        if (userService.getCurrentUser() != null) {
            storeSettingsStringsOnServer(settingsStrings, onSettingsStoredCallback);
        } else {
            onSettingsStoredCallback.onSuccess();
        }
    }

    @Override
    public void retrieveSettingsJson(AsyncCallback<SettingsJsons> asyncCallback) {
        if (userService.getCurrentUser() == null) {
            SettingsStrings settingsStrings = retrieveSettingsStringsFromLocalStorage();
            asyncCallback.onSuccess(convertToSettingsJson(settingsStrings));
        } else {
            retrieveSettingsStringsFromServer(new AsyncCallback<SettingsStrings>() {
                
                @Override
                public void onSuccess(SettingsStrings serverSettingsStrings) {
                    serverSettingsStrings = syncLocalStorageAndServer(serverSettingsStrings);
                    asyncCallback.onSuccess(convertToSettingsJson(serverSettingsStrings));
                }

                @Override
                public void onFailure(Throwable caught) {
                    asyncCallback.onFailure(caught);
                }
            });
        }
    }
    
    private SettingsStrings syncLocalStorageAndServer(SettingsStrings serverSettingsStrings) {
        if(serverSettingsStrings.getGlobalSettingsString() == null && serverSettingsStrings.getContextSpecificSettingsString() == null) {
            SettingsStrings localStorageSettingsStrings = retrieveSettingsStringsFromLocalStorage();
            if(localStorageSettingsStrings.getGlobalSettingsString() != null || localStorageSettingsStrings.getContextSpecificSettingsString() != null) {
                storeSettingsStringsOnServer(localStorageSettingsStrings, new OnSettingsStoredCallback() {
                    
                    @Override
                    public void onSuccess() {
                        //nothing to do
                    }
                    
                    @Override
                    public void onError(Throwable caught) {
                        //nothing to do
                    }
                });
            }
            serverSettingsStrings = localStorageSettingsStrings;
        } else {
            storeSettingsStringsOnLocalStorage(serverSettingsStrings);
        }
        return serverSettingsStrings;
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
