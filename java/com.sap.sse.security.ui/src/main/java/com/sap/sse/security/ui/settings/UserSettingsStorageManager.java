package com.sap.sse.security.ui.settings;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.CallbacksJoinerHelper;
import com.sap.sse.gwt.client.shared.perspective.DefaultSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * 
 * @author Vlad
 *
 * @param <R>
 * @param <S>
 */
public class UserSettingsStorageManager<PS extends Settings> implements SettingsStorageManager<PS> {
    
    private final String storageGlobalKey;
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

    public UserSettingsStorageManager(UserService userService, String globalDefinitionId, String... contextDefinitionParameters) {
        this.userService = userService;
        this.storageGlobalKey = globalDefinitionId;
        this.storageContextSpecificKey = this.storageGlobalKey + "#" + buildContextDefinitionId(contextDefinitionParameters);
    }
    
    public void storeGlobalSettings(PerspectiveCompositeSettings<PS> globalSettings) {
        String serializedSettings = jsonSerializer.serializeToString(globalSettings);
        storeGlobalSettingsJsonOnServer(serializedSettings);
        storeGlobalSettingsJsonOnLocalStorage(serializedSettings);
    }
    
    public void storeContextSpecificSettings(PerspectiveCompositeSettings<PS> contextSpecificSettings) {
        String serializedSettings = jsonSerializer.serializeToString(contextSpecificSettings);
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

    public void retrieveDefaultSettings(PerspectiveCompositeSettings<PS> defaultSettings, final DefaultSettingsLoadedCallback<PS> asyncCallback) {
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
        });
    }
    
    private void continueRetrieveDefaultSettings(
            UserSettingsStorageManager<PS>.SettingsJsonRetrievement settingsJsonRetrievement, DefaultSettingsLoadedCallback<PS> callback) {
        PerspectiveCompositeSettings<PS> defaultSettings = settingsJsonRetrievement.getDefaultSettings();
        
        // has been any global settings from server retrieved? yes => apply as default and override LocalStorage
        if(settingsJsonRetrievement.getGlobalSettingsJson() != null) {
            defaultSettings = jsonSerializer.deserialize(defaultSettings, settingsJsonRetrievement.getGlobalSettingsJson());
            storeGlobalSettingsJsonOnLocalStorage(settingsJsonRetrievement.getGlobalSettingsJson());
        } else {
            String localStorageGlobalSettingsJson = retrieveGlobalSettingsJsonFromLocalStorage();
            if(localStorageGlobalSettingsJson != null) {
                //server has no global settings, local storage has => apply local storage settings and store them on server
                defaultSettings = jsonSerializer.deserialize(defaultSettings, localStorageGlobalSettingsJson);
                storeGlobalSettingsJsonOnServer(localStorageGlobalSettingsJson);
            }
        }
        
        // has been any context specific settings from server retrieved? yes => apply as default and override LocalStorage
        if(settingsJsonRetrievement.getContextSpecificSettingsJson() != null) {
            defaultSettings = jsonSerializer.deserialize(defaultSettings, settingsJsonRetrievement.getContextSpecificSettingsJson());
            storeContextSpecificSettingsJsonOnLocalStorage(settingsJsonRetrievement.getContextSpecificSettingsJson());
        } else {
            String localStorageContextSpecificSettingsJson = retrieveContextSpecificSettingsJsonFromLocalStorage();
            if(localStorageContextSpecificSettingsJson != null) {
                //server has no context specific settings, local storage has => apply local storage settings and store them on server
                defaultSettings = jsonSerializer.deserialize(defaultSettings, localStorageContextSpecificSettingsJson);
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

    private PerspectiveCompositeSettings<PS> retrieveDefaultSettingsFromUrl(PerspectiveCompositeSettings<PS> defaultSettings) {
        return urlSerializer.deserializeFromCurrentLocation(defaultSettings);
    }
    
    private class SettingsJsonRetrievement extends CallbacksJoinerHelper<String, String> {
        private PerspectiveCompositeSettings<PS> defaultSettings;
        
        public SettingsJsonRetrievement(PerspectiveCompositeSettings<PS> defaultSettings) {
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
        public PerspectiveCompositeSettings<PS> getDefaultSettings() {
            return defaultSettings;
        }
    }
    
    public Throwable getLastError() {
        return lastError;
    }
    
    private static String buildContextDefinitionId(String[] contextDefinitionParameters) {
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
