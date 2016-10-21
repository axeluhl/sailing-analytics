package com.sap.sse.gwt.client.shared.defaultsettings;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;

/**
 * 
 * @author Vlad
 *
 * @param <R>
 * @param <S>
 */
public class DefaultSettingsStorage<PS extends Settings> {
    
    private static final String SETTINGS_STORAGE_KEY_PREFIX = "SETTINGS-";
    private final String storageRootPerspectiveKey;
    
    //TODO replace userService with real implementation
    private WithAuthenticationManager clientFactory = mockWithAuthenticationManager();
    
    //TODO replace userService with real implementation
    private UserService userService = mockUserService();
    
    private final SettingsToJsonSerializerGWT serializer = new SettingsToJsonSerializerGWT();
    
    public DefaultSettingsStorage(String rootPerspectiveId) {
        storageRootPerspectiveKey = SETTINGS_STORAGE_KEY_PREFIX + rootPerspectiveId;
    }
    
    public void storeDefaultSettings(PerspectiveCompositeSettings<PS> rootPerspectiveSettings) {
        String serializedDefaultSettings = serializer.serializeToString(rootPerspectiveSettings);
        if(clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            storeDefaultSettingsOnServer(serializedDefaultSettings);
        }
        storeDefaultSettingsOnLocalStorage(serializedDefaultSettings);
    }
    
    private void storeDefaultSettingsOnLocalStorage(String serializedDefaultSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            localStorage.removeItem(storageRootPerspectiveKey);
            localStorage.setItem(storageRootPerspectiveKey, serializedDefaultSettings);
        }
    }

    private void storeDefaultSettingsOnServer(String serializedDefaultSettings) {
        userService.setPreference(storageRootPerspectiveKey, serializedDefaultSettings);
    }

    public void getDefaultSettings(PerspectiveCompositeSettings<PS> defaultSettings, final AsyncCallback<PerspectiveCompositeSettings<PS>> asyncCallback) {
        defaultSettings = retrieveDefaultSettingsFromLocalStorage(defaultSettings);
        
        AsyncCallback<PerspectiveCompositeSettings<PS>> internAsyncCallback = new AsyncCallback<PerspectiveCompositeSettings<PS>>() {
            @Override
            public void onFailure(Throwable caught) {
                asyncCallback.onFailure(caught);
            }
            
            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS> defaultSettings) {
                defaultSettings = retrieveDefaultSettingsFromUrl(defaultSettings);
                asyncCallback.onSuccess(defaultSettings);
            }
        };
        
        if(clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            retrieveDefaultSettingsFromServer(defaultSettings, internAsyncCallback);
        } else {
            internAsyncCallback.onSuccess(defaultSettings);
        }
    }
    
    private PerspectiveCompositeSettings<PS> retrieveDefaultSettingsFromLocalStorage(PerspectiveCompositeSettings<PS> defaultSettings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String serializedSettings = localStorage.getItem(storageRootPerspectiveKey);
            defaultSettings = serializer.deserialize(defaultSettings, serializedSettings);
        }
        return defaultSettings;
    }

    private void retrieveDefaultSettingsFromServer(final PerspectiveCompositeSettings<PS> defaultSettings, final AsyncCallback<PerspectiveCompositeSettings<PS>> asyncCallback) {
        userService.getPreference(storageRootPerspectiveKey, defaultSettings, asyncCallback);
    }

    private PerspectiveCompositeSettings<PS> retrieveDefaultSettingsFromUrl(PerspectiveCompositeSettings<PS> defaultSettings) {
        // TODO implement url parsing
        return defaultSettings;
    }

    //TODO replace mockups with real implementation
    private interface UserService {
        /**
         * Loads the {@link #getCurrentUser() current user}'s preference with the given {@link String key} from server.
         * Because the preference is persisted as JSON on server, the loaded data will be deserialized into the provided
         * {@link GenericSerializableSettings} instance.
         * 
         * @param key
         *            key of the preference to load
         * @param emptyInstance
         *            an empty {@link GenericSerializableSettings} instance, where the loaded data is deserialized to
         * @param callback
         *            {@link AsyncCallback} for GWT RPC call
         *            
         * @see GenericSerializableSettings
         * @see AbstractGenericSerializableSettings
         */
        public <T extends SettingsMap> void getPreference(String key, final T emptyInstance,
                final AsyncCallback<T> callback);
        
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
            public <T extends SettingsMap> void getPreference(String key, final T emptyInstance,
                    final AsyncCallback<T> callback) {
                // TODO Auto-generated method stub
                
            }
        };
    }

}
