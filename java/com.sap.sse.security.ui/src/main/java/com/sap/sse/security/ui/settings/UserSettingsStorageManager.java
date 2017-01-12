package com.sap.sse.security.ui.settings;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.CallbacksJoinerHelper;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * The settings are stored in HTML5 LocalStorage, as well as on server in the user's
 * account, in case the user is logged.
 * 
 * @author Vladislav Chumak
 *
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all the settings for itself and its subcomponents
 * @see SettingsStorageManager
 */
public class UserSettingsStorageManager<S extends Settings> implements SettingsStorageManager<S> {
    
    /**
     * The key which is associated with the global settings. Different keys will cause multiple/different settings instances stored in the storage.
     */
    private final String storageGlobalKey;
    
    /**
     * The key which is used to store the context specific settings. Each context with own context specific settings must have a unique key.
     */
    private final String storageContextSpecificKey;
    
    private Throwable lastError = null;
    
    private UserService userService;
    
    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();
    private final SettingsToUrlSerializer urlSerializer = new SettingsToUrlSerializer();
    
    /**
     * This is used, to ensure that only once the data is loaded remote, if a user logs in later, he must refresh, to
     * avoid "complicated problems"
     */
    private boolean initialUserSetting = false;

    /**
     * 
     * @param userService The {@link UserService} which is used for settings storage on server when the current user is logged in
     * @param globalDefinitionId  The key which is associated with the global settings. Different keys will cause multiple/different settings instances stored in the storage.
     * @param contextDefinitionId The key which is used to store the context specific settings. Each context with own context specific settings must have a unique key.
     */
    public UserSettingsStorageManager(UserService userService, String globalDefinitionId, String contextDefinitionId) {
        this.userService = userService;
        this.storageGlobalKey = globalDefinitionId;
        this.storageContextSpecificKey = this.storageGlobalKey + "#" + contextDefinitionId;
    }
    
    /**
     * {@inheritDoc}
     */
    public void storeGlobalSettings(S globalSettings) {
        String serializedSettings = serializeToJson(globalSettings);
        storeGlobalSettingsJsonOnServer(serializedSettings);
        storeGlobalSettingsJsonOnLocalStorage(serializedSettings);
    }
    
    /**
     * {@inheritDoc}
     */
    public void storeContextSpecificSettings(S contextSpecificSettings) {
        String serializedSettings = serializeToJson(contextSpecificSettings);
        storeContextSpecificSettingsJsonOnServer(serializedSettings);
        storeContextSpecificSettingsJsonOnLocalStorage(serializedSettings);
    }
    
    private void storeContextSpecificSettingsJsonOnLocalStorage(String serializedContextSpecificSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageContextSpecificKey);
            localStorage.setItem(storageContextSpecificKey, serializedContextSpecificSettings);
        }
    }

    private void storeContextSpecificSettingsJsonOnServer(String serializedContextSpecificSettings) {
        if(userService.getCurrentUser() != null) {
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
        if(userService.getCurrentUser() != null) {
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
                        retrieveGlobalSettingsJsonFromServer(globalSettingsAsyncCallback);

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
                        retrieveContextSpecificSettingsJsonFromServer(contextSpecificSettingsAsyncCallback);
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
            UserSettingsStorageManager<S>.SettingsJsonRetrievement settingsJsonRetrievement, OnSettingsLoadedCallback<S> callback) {
        S defaultSettings = settingsJsonRetrievement.getDefaultSettings();
        
        // has been any global settings from server retrieved? yes => apply as default and override LocalStorage
        if(settingsJsonRetrievement.getGlobalSettingsJson() != null) {
            defaultSettings = deserializeFromJson(defaultSettings, settingsJsonRetrievement.getGlobalSettingsJson());
            storeGlobalSettingsJsonOnLocalStorage(settingsJsonRetrievement.getGlobalSettingsJson());
        } else {
            String localStorageGlobalSettingsJson = retrieveGlobalSettingsJsonFromLocalStorage();
            if(localStorageGlobalSettingsJson != null) {
                //server has no global settings, local storage has => apply local storage settings and store them on server
                defaultSettings = deserializeFromJson(defaultSettings, localStorageGlobalSettingsJson);
                storeGlobalSettingsJsonOnServer(localStorageGlobalSettingsJson);
            }
        }
        
        // has been any context specific settings from server retrieved? yes => apply as default and override LocalStorage
        if(settingsJsonRetrievement.getContextSpecificSettingsJson() != null) {
            defaultSettings = deserializeFromJson(defaultSettings, settingsJsonRetrievement.getContextSpecificSettingsJson());
            storeContextSpecificSettingsJsonOnLocalStorage(settingsJsonRetrievement.getContextSpecificSettingsJson());
        } else {
            String localStorageContextSpecificSettingsJson = retrieveContextSpecificSettingsJsonFromLocalStorage();
            if(localStorageContextSpecificSettingsJson != null) {
                //server has no context specific settings, local storage has => apply local storage settings and store them on server
                defaultSettings = deserializeFromJson(defaultSettings, localStorageContextSpecificSettingsJson);
                storeContextSpecificSettingsJsonOnServer(localStorageContextSpecificSettingsJson);
            }
        }
        
        //URL settings have highest precedence
        defaultSettings = retrieveDefaultSettingsFromUrl(defaultSettings);
        
        if(settingsJsonRetrievement.isErrorOccurred()) {
            callback.onError(settingsJsonRetrievement.getCaught(), defaultSettings);
        } else {
            callback.onSuccess(defaultSettings);
        }
    }
    
    @SuppressWarnings("unchecked")
    private S deserializeFromCurrentUrl(S defaultSettings) {
        if(defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) urlSerializer.deserializeFromCurrentLocation((GenericSerializableSettings) defaultSettings);
        } else if(defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) urlSerializer.deserializeFromCurrentLocation((SettingsMap) defaultSettings);
        }
        return defaultSettings;
    }
    
    @SuppressWarnings("unchecked")
    private S deserializeFromJson(S defaultSettings, String jsonToDeserialize) {
        if(defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) jsonSerializer.deserialize((GenericSerializableSettings) defaultSettings, jsonToDeserialize);
        } else if(defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) jsonSerializer.deserialize((SettingsMap) defaultSettings, jsonToDeserialize);
        }
        return defaultSettings;
    }
    
    private String serializeToJson(S settingsToSerialize) {
        String serializedSettings = "";
        if(settingsToSerialize instanceof GenericSerializableSettings) {
            serializedSettings = jsonSerializer.serializeToString((GenericSerializableSettings) settingsToSerialize);
        } else if(settingsToSerialize instanceof SettingsMap) {
            serializedSettings = jsonSerializer.serializeToString((SettingsMap) settingsToSerialize);
        }
        return serializedSettings;
    }

    private String retrieveGlobalSettingsJsonFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            return localStorage.getItem(storageGlobalKey);
        }
        return null;
    }
    
    private String retrieveContextSpecificSettingsJsonFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            return localStorage.getItem(storageContextSpecificKey);
        }
        return null;
    }

    private void retrieveGlobalSettingsJsonFromServer(AsyncCallback<String> asyncCallback) {
        userService.getPreference(storageGlobalKey, asyncCallback);
    }
    
    private void retrieveContextSpecificSettingsJsonFromServer(AsyncCallback<String> asyncCallback) {
        userService.getPreference(storageContextSpecificKey, asyncCallback);
    }

    private S retrieveDefaultSettingsFromUrl(S defaultSettings) {
        return deserializeFromCurrentUrl(defaultSettings);
    }
    
    /**
     * Helper class to leverage two parallel server calls in order to retrieve global and context specific settings
     * without additional unnecessary server-roundtrips and provide the results when both call results have been received.
     * 
     * @author Vladislav Chumak
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
    
    /**
     * Utility method used to build a {@link AbstractComponentContextWithSettingsStorage#contextDefinitionId context definition id}
     * in order to construct an object of this class.
     * 
     * @param contextDefinitionParameters The parameters which shape the context
     * @return The generated context definition id from the provided parameters
     */
    public static String buildContextDefinitionId(String... contextDefinitionParameters) {
        StringBuilder str = new StringBuilder("");
        boolean first = true;
        for(String contextDefinitionParameter : contextDefinitionParameters) {
            if(first) {
                first = false;
            } else {
                str.append(",");
            }
            if(contextDefinitionParameter != null) {
                str.append(contextDefinitionParameter);
            }
        }
        return str.toString();
    }
    
}
