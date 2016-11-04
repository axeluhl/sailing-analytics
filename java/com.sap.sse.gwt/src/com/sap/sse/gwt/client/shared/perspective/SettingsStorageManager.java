package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

/**
 * 
 * @author Vlad
 *
 * @param <R>
 * @param <S>
 */
public class SettingsStorageManager<PS extends Settings> {
    
    private final String storageGlobalKey;
    private final String storageContextSpecificKey;
    private Throwable lastError = null;

    
    //TODO replace userService with real implementation
    private WithAuthenticationManager clientFactory = mockWithAuthenticationManager();
    
    //TODO replace userService with real implementation
    private UserService userService = mockUserService();
    
    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();
    private final SettingsToUrlSerializer urlSerializer = new SettingsToUrlSerializer();
    
    public SettingsStorageManager(String globalDefinitionId, String contextDefinitionId) {
        this.storageGlobalKey = "#" + globalDefinitionId;
        this.storageContextSpecificKey = this.storageGlobalKey + "~" + contextDefinitionId;
    }
    
    public void storeGlobalSettings(PerspectiveCompositeSettings<PS> globalSettings) {
        String serializedSettings = jsonSerializer.serializeToString(globalSettings);
        if(clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            storeGlobalSettingsOnServer(serializedSettings);
        }
        storeGlobalSettingsOnLocalStorage(serializedSettings);
    }
    
    public void storeContextSpecificSettings(PerspectiveCompositeSettings<PS> contextSpecificSettings) {
        String serializedSettings = jsonSerializer.serializeToString(contextSpecificSettings);
        if(clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            storeContextSpecificSettingsOnServer(serializedSettings);
        }
        storeContextSpecificSettingsOnLocalStorage(serializedSettings);
    }
    
    private void storeContextSpecificSettingsOnLocalStorage(String serializedContextSpecificSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageContextSpecificKey);
            localStorage.setItem(storageContextSpecificKey, serializedContextSpecificSettings);
        }
    }

    private void storeContextSpecificSettingsOnServer(String serializedContextSpecificSettings) {
        userService.setPreference(storageContextSpecificKey, serializedContextSpecificSettings);
    }

    private void storeGlobalSettingsOnLocalStorage(String serializedGlobalSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageGlobalKey);
            localStorage.setItem(storageGlobalKey, serializedGlobalSettings);
        }
    }

    private void storeGlobalSettingsOnServer(String serializedGlobalSettings) {
        userService.setPreference(storageGlobalKey, serializedGlobalSettings);
    }

    public void retrieveDefaultSettings(PerspectiveCompositeSettings<PS> defaultSettings, final DefaultSettingsLoadedCallback<PS> asyncCallback) {
        defaultSettings = retrieveGlobalSettingsFromLocalStorage(defaultSettings);
        defaultSettings = retrieveContextSpecificSettingsFromLocalStorage(defaultSettings);
        
        defaultSettings = retrieveDefaultSettingsFromUrl(defaultSettings);
        final SettingsJsonRetrievement settingsJsonRetrievement = new SettingsJsonRetrievement(defaultSettings);
        if(clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
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
                    if(settingsJsonRetrievement.hasAllCallbacksReceived()) {
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
                    settingsJsonRetrievement.receiveContextSpecificSettingsJson(contextSpecificSettingsJson);
                    if(settingsJsonRetrievement.hasAllCallbacksReceived()) {
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
    
    private void continueRetrieveDefaultSettings(
            SettingsStorageManager<PS>.SettingsJsonRetrievement settingsJsonRetrievement, DefaultSettingsLoadedCallback<PS> callback) {
        PerspectiveCompositeSettings<PS> defaultSettings = settingsJsonRetrievement.getDefaultSettings();
        if(settingsJsonRetrievement.getGlobalSettingsJson() != null) {
            defaultSettings = jsonSerializer.deserialize(defaultSettings, settingsJsonRetrievement.getGlobalSettingsJson());
        }
        if(settingsJsonRetrievement.getContextSpecificSettingsJson() != null) {
            defaultSettings = jsonSerializer.deserialize(defaultSettings, settingsJsonRetrievement.getContextSpecificSettingsJson());
        }
        defaultSettings = urlSerializer.deserializeFromCurrentLocation(defaultSettings);
        
        if(settingsJsonRetrievement.isErrorOccurred()) {
            callback.onError(settingsJsonRetrievement.getCaught(), defaultSettings);
        } else {
            callback.onSuccess(defaultSettings);
        }
    }

    private PerspectiveCompositeSettings<PS> retrieveGlobalSettingsFromLocalStorage(PerspectiveCompositeSettings<PS> defaultSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String serializedSettings = localStorage.getItem(storageGlobalKey);
            defaultSettings = jsonSerializer.deserialize(defaultSettings, serializedSettings);
        }
        return defaultSettings;
    }
    
    private PerspectiveCompositeSettings<PS> retrieveContextSpecificSettingsFromLocalStorage(PerspectiveCompositeSettings<PS> defaultSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String serializedSettings = localStorage.getItem(storageContextSpecificKey);
            defaultSettings = jsonSerializer.deserialize(defaultSettings, serializedSettings);
        }
        return defaultSettings;
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
    
    public Throwable getLastServerError() {
        return lastError;
    }
    
    public String serializeSettings(PerspectiveCompositeSettings<PS> settings) {
        return jsonSerializer.serializeToString(settings);
    }
    
    public PerspectiveCompositeSettings<PS> deserializeSettings(String serializedToJsonSettings, PerspectiveCompositeSettings<PS> defaultSettings) {
        return jsonSerializer.deserialize(defaultSettings, serializedToJsonSettings);
    }

    //TODO replace mockups with real implementation
    private interface UserService {
        /**
         * Loads the {@link #getCurrentUser() current user}'s preference with the given {@link String key} from server.
         * 
         * @param key
         *            key of the preference to load
         * @param callback
         *            {@link AsyncCallback} for GWT RPC call
         *            
         * @see GenericSerializableSettings
         * @see AbstractGenericSerializableSettings
         */
        public void getPreference(String key,
                final AsyncCallback<String> callback);
        
        /**
         * Sets the {@link #getCurrentUser() current user}'s preference with the given {@link String key} on server.
         * Because preferences are persisted as JSON, the provided {@link GenericSerializableSettings} instance
         * will be serialized before it is sent to the server.
         * 
         * @param key
         *            key of the preference to set
         * @param instance
         *            {@link GenericSerializableSettings} instance containing the preference value
         *            
         * @see GenericSerializableSettings
         * @see AbstractGenericSerializableSettings
         */
        public void setPreference(String key, String serializedSettings);
    }
    
    private interface WithAuthenticationManager {
        
        /**
         * @return the {@link AuthenticationManager}
         */
        AuthenticationManager getAuthenticationManager();
    }
    
    private interface AuthenticationManager {
        /**
         * Provide the {@link AuthenticationContext} for the current user 
         * 
         * @return an {@link AuthenticationContext} instance
         */
        AuthenticationContext getAuthenticationContext();
    }
    
    private interface AuthenticationContext {
        
        /**
         * Determines if there is a logged in user.
         * 
         * @return <code>true</code> if a user is logged in, <code>false</code> otherwise
         */
        boolean isLoggedIn();
    }
    
    private static WithAuthenticationManager mockWithAuthenticationManager() {
        return new WithAuthenticationManager() {
            
            @Override
            public AuthenticationManager getAuthenticationManager() {
                return new AuthenticationManager() {
                    
                    @Override
                    public AuthenticationContext getAuthenticationContext() {
                        return new AuthenticationContext() {
                            @Override
                            public boolean isLoggedIn() {
                                return false;
                            }
                        };
                    }
                };
            }
        };
    }
    
    private static UserService mockUserService() {
        return new UserService() {
            
            @Override
            public void setPreference(String key, String serializedSettings) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void getPreference(String key,
                    final AsyncCallback<String> callback) {
                // TODO Auto-generated method stub
                
            }
        };
    }
    
}
