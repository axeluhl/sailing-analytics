package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;
import com.sap.sse.gwt.client.shared.settings.SettingsStrings;
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
public class UserSettingsStorageManager<S extends Settings> implements SettingsStorageManager<S> {
    
    /**
     * The pipeline used for the settings construction.
     */
    protected final SettingsBuildingPipeline settingsBuildingPipeline;

    /**
     * The key which is associated with the User Settings. Different keys will cause multiple/different settings
     * instances to be stored in the storage.
     */
    private final String storageGlobalKey;

    /**
     * The key which is associated with the Document Settings. Different keys will cause multiple/different settings
     * instances to be stored in the storage.
     */
    private final String storageContextSpecificKey;

    /**
     * The userService instance which is used for server communication regarding settings loading and storing
     */
    private UserService userService;

    /**
     * Indicates whether the settings have been already loaded for the first time in order to maintain a single attachment of {@link UserStatusEventHandler}.
     */
    private boolean initialUserSetting = false;
    
    /**
     * Handler which is currently attached for listening the user's login state
     */
    private UserStatusEventHandler userStatusEventHandler = null;
    
    /**
     * Callbacks which are waiting to be called when persistent settings representation has loaded.
     */
    private Queue<AsyncCallback<SettingsJsons>> retrieveSettingsCallbacksQueue = new LinkedList<>();
    
    /**
     * Constructs the instance with a custom {@link SettingsBuildingPipeline}.
     * 
     * @param userService The service which is used for server-side settings storage
     * @param storageDefinitionId The definition for User Settings and Document Settings storage keys
     * @param settingsBuildingPipeline The custom pipeline to be used for settings construction
     */
    protected UserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId, SettingsBuildingPipeline settingsBuildingPipeline) {
        this.settingsBuildingPipeline = settingsBuildingPipeline;
        this.userService = userService;
        this.storageGlobalKey = storageDefinitionId.generateStorageGlobalKey();
        this.storageContextSpecificKey = storageDefinitionId.generateStorageContextSpecificKey();
    }
    
    /**
     * Constructs the instance with {@link UserSettingsBuildingPipeline}.
     * 
     * @param userService The service which is used for server-side settings storage
     * @param storageDefinitionId The definition for User Settings and Document Settings storage keys
     */
    public UserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId) {
        this(userService, storageDefinitionId, new UserSettingsBuildingPipeline());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsStore() {
        return true;
    }

    private void storeSettingsStringsOnLocalStorage(SettingsStrings settingsStrings, boolean storeGlobalSettings, boolean storeContextSpecificSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            if(storeGlobalSettings) {
                localStorage.removeItem(storageGlobalKey);
                if(settingsStrings.getGlobalSettingsString() != null) {
                    localStorage.setItem(storageGlobalKey, settingsStrings.getGlobalSettingsString());
                }
            }
            if(storeContextSpecificSettings) {
                localStorage.removeItem(storageContextSpecificKey);
                if(settingsStrings.getContextSpecificSettingsString() != null) {
                    localStorage.setItem(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString());
                }
            }
        }
    }

    private void storeSettingsStringsOnServer(SettingsStrings settingsStrings, final OnSettingsStoredCallback onSettingsStoredCallback, boolean storeGlobalSettings, boolean storeContextSpecificSettings) {
        Map<String, String> keyValuePairs = new HashMap<>();
        if(storeGlobalSettings) {
            keyValuePairs.put(storageGlobalKey, settingsStrings.getGlobalSettingsString());
        }
        if(storeContextSpecificSettings) {
            keyValuePairs.put(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString());
        }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveDefaultSettings(final S defaultSettings, final OnSettingsLoadedCallback<S> callback) {
        retrieveSettingsJsons(new AsyncCallback<SettingsJsons>() {

            @Override
            public void onFailure(Throwable caught) {
                SettingsStrings localStorageSettings = retrieveSettingsStringsFromLocalStorage();
                S newDefaultSettings = settingsBuildingPipeline.getSettingsObject(defaultSettings, localStorageSettings);
                callback.onError(caught, newDefaultSettings);
            }

            @Override
            public void onSuccess(SettingsJsons settingsJsons) {
                S newDefaultSettings = settingsBuildingPipeline.getSettingsObject(defaultSettings, settingsJsons);
                callback.onSuccess(newDefaultSettings);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if(userStatusEventHandler != null) {
            userService.removeUserStatusEventHandler(userStatusEventHandler);
        }
    }
    
    private SettingsStrings retrieveSettingsStringsFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String globalSettings = localStorage.getItem(storageGlobalKey);
            String contextSpecificSettings = localStorage.getItem(storageContextSpecificKey);
            return new SettingsStrings(globalSettings, contextSpecificSettings);
        }
        return new SettingsStrings(null, null);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JSONValue settingsToJSON(Settings newSettings, PipelineLevel pipelineLevel, List<String> path) {
        return settingsBuildingPipeline.getJsonObject(newSettings, pipelineLevel, path);
    }
    
    private void storeSettingsJsons(SettingsJsons settingsJsons, OnSettingsStoredCallback onSettingsStoredCallback, boolean storeGlobalSettings, boolean storeContextSpecificSettings) {
        SettingsStrings settingsStrings = settingsBuildingPipeline.getSettingsStringConverter().convertToSettingsStrings(settingsJsons);
        storeSettingsStringsOnLocalStorage(settingsStrings, storeGlobalSettings, storeContextSpecificSettings);
        if (userService.getCurrentUser() != null) {
            storeSettingsStringsOnServer(settingsStrings, onSettingsStoredCallback, storeGlobalSettings, storeContextSpecificSettings);
        } else {
            onSettingsStoredCallback.onSuccess();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void storeGlobalSettingsJson(JSONObject globalSettingsJson,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        SettingsJsons settingsJsons = new SettingsJsons(globalSettingsJson, null);
        storeSettingsJsons(settingsJsons, onSettingsStoredCallback, true, false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void storeContextSpecificSettingsJson(JSONObject contextSpecificSettingsJson,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        SettingsJsons settingsJsons = new SettingsJsons(null, contextSpecificSettingsJson);
        storeSettingsJsons(settingsJsons, onSettingsStoredCallback, false, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveSettingsJsons(AsyncCallback<SettingsJsons> asyncCallback) {
        retrieveSettingsCallbacksQueue.add(asyncCallback);
        if(userStatusEventHandler == null) {
            this.userStatusEventHandler = new UserStatusEventHandler() {
    
                @Override
                public void onUserStatusChange(UserDTO user) {
                    //call this method always on user status change event in order to cause sync between local storage and server
                    if(!initialUserSetting) {
                        initialUserSetting = true;
                        requestSettingsJsons();
                    }
                }
            };
            userService.addUserStatusEventHandler(userStatusEventHandler, true);
        } else if(initialUserSetting) {
            requestSettingsJsons();
        }
    }

    private void requestSettingsJsons() {
        if (userService.getCurrentUser() == null) {
            SettingsStrings settingsStrings = retrieveSettingsStringsFromLocalStorage();
            AsyncCallback<SettingsJsons> asyncCallback;
            while((asyncCallback = retrieveSettingsCallbacksQueue.poll()) != null) {
                asyncCallback.onSuccess(settingsBuildingPipeline.getSettingsStringConverter().convertToSettingsJson(settingsStrings));
            }
        } else {
            retrieveSettingsStringsFromServer(new AsyncCallback<SettingsStrings>() {
                
                @Override
                public void onSuccess(SettingsStrings serverSettingsStrings) {
                    serverSettingsStrings = syncLocalStorageAndServer(serverSettingsStrings);
                    AsyncCallback<SettingsJsons> asyncCallback;
                    while((asyncCallback = retrieveSettingsCallbacksQueue.poll()) != null) {
                        asyncCallback.onSuccess(settingsBuildingPipeline.getSettingsStringConverter().convertToSettingsJson(serverSettingsStrings));
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    AsyncCallback<SettingsJsons> asyncCallback;
                    while((asyncCallback = retrieveSettingsCallbacksQueue.poll()) != null) {
                        asyncCallback.onFailure(caught);
                    }
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
                }, true, true);
            }
            serverSettingsStrings = localStorageSettingsStrings;
        } else {
            storeSettingsStringsOnLocalStorage(serverSettingsStrings, true, true);
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
