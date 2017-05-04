package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;
import com.sap.sse.gwt.client.shared.settings.SettingsStrings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * The settings are stored in HTML5 LocalStorage, as well as on server in the user's account, in case the user is
 * logged.
 * 
 * @author Vladislav Chumak
 *
 * @see SettingsStorageManager
 */
public class UserSettingsStorageManager implements SettingsStorageManager<SettingsStrings> {
    
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
    private Queue<OnSettingsLoadedCallback<SettingsStrings>> retrieveSettingsCallbacksQueue = new LinkedList<>();
    
    /**
     * @param userService The service which is used for server-side settings storage
     * @param storageDefinitionId The definition for User Settings and Document Settings storage keys
     */
    public UserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId) {
        this.userService = userService;
        this.storageGlobalKey = storageDefinitionId.generateStorageGlobalKey();
        this.storageContextSpecificKey = storageDefinitionId.generateStorageContextSpecificKey();
    }
    
    @Override
    public void storeSettingsRepresentation(SettingsStrings settingsStrings, OnSettingsStoredCallback onSettingsStoredCallback) {
        storeSettingsStringsOnLocalStorage(settingsStrings);
        if (userService.getCurrentUser() != null) {
            storeSettingsStringsOnServer(settingsStrings, onSettingsStoredCallback);
        } else {
            onSettingsStoredCallback.onSuccess();
        }
    }
    
    private void storeSettingsStringsOnLocalStorage(SettingsStrings settingsStrings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            if(settingsStrings.getGlobalSettingsString() != null) {
                localStorage.removeItem(storageGlobalKey);
                localStorage.setItem(storageGlobalKey, settingsStrings.getGlobalSettingsString());
            }
            if(settingsStrings.getContextSpecificSettingsString() != null) {
                localStorage.removeItem(storageContextSpecificKey);
                localStorage.setItem(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString());
            }
        }
    }

    private void storeSettingsStringsOnServer(SettingsStrings settingsStrings, final OnSettingsStoredCallback onSettingsStoredCallback) {
        AsyncCallback<Void> asyncCallback = new AsyncCallback<Void>() {
            
            @Override
            public void onFailure(Throwable caught) {
                onSettingsStoredCallback.onError(caught);
            }
            
            @Override
            public void onSuccess(Void result) {
                onSettingsStoredCallback.onSuccess();
            }
        };
        
        if(settingsStrings.getContextSpecificSettingsString() == null || settingsStrings.getGlobalSettingsString() == null) {
            if(settingsStrings.getContextSpecificSettingsString() != null) {
                userService.setPreference(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString(), asyncCallback);
            }
            if(settingsStrings.getGlobalSettingsString() != null) {
                userService.setPreference(storageGlobalKey, settingsStrings.getGlobalSettingsString(), asyncCallback);
            }
        } else {
            Map<String, String> keyValuePairs = new HashMap<>();
            if(settingsStrings.getGlobalSettingsString() != null) {
                keyValuePairs.put(storageGlobalKey, settingsStrings.getGlobalSettingsString());
            }
            if(settingsStrings.getContextSpecificSettingsString() != null) {
                keyValuePairs.put(storageContextSpecificKey, settingsStrings.getContextSpecificSettingsString());
            }
            userService.setPreferences(keyValuePairs, asyncCallback);
        }
        
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
    public void retrieveSettingsRepresentation(OnSettingsLoadedCallback<SettingsStrings> callback) {
        retrieveSettingsCallbacksQueue.add(callback);
        if(userStatusEventHandler == null) {
            this.userStatusEventHandler = new UserStatusEventHandler() {
    
                @Override
                public void onUserStatusChange(UserDTO user) {
                    //call this method always on user status change event in order to cause sync between local storage and server
                    if(!initialUserSetting) {
                        initialUserSetting = true;
                        retrieveSettingsStringsFromServerOrLocalStorage();
                    }
                }
            };
            userService.addUserStatusEventHandler(userStatusEventHandler, true);
        } else if(initialUserSetting) {
            retrieveSettingsStringsFromServerOrLocalStorage();
        }
    }

    private void retrieveSettingsStringsFromServerOrLocalStorage() {
        if (userService.getCurrentUser() == null) {
            SettingsStrings settingsStrings = retrieveSettingsStringsFromLocalStorage();
            OnSettingsLoadedCallback<SettingsStrings> callback;
            while((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                callback.onSuccess(settingsStrings);
            }
        } else {
            retrieveSettingsStringsFromServer(new AsyncCallback<SettingsStrings>() {
                
                @Override
                public void onSuccess(SettingsStrings serverSettingsStrings) {
                    serverSettingsStrings = syncLocalStorageAndServer(serverSettingsStrings);
                    OnSettingsLoadedCallback<SettingsStrings> callback;
                    while((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                        callback.onSuccess(serverSettingsStrings);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    SettingsStrings fallbackSettingsStrings = retrieveSettingsStringsFromLocalStorage();
                    OnSettingsLoadedCallback<SettingsStrings> callback;
                    while((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                        callback.onError(caught, fallbackSettingsStrings);
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
                });
            }
            serverSettingsStrings = localStorageSettingsStrings;
        } else {
            storeSettingsStringsOnLocalStorage(serverSettingsStrings);
        }
        return serverSettingsStrings;
    }
    
}
