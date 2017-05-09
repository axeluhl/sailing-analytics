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
import com.sap.sse.gwt.client.shared.settings.PersistableSettingsRepresentations;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;
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
public class UserSettingsStorageManager implements SettingsStorageManager<String> {

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
     * Indicates whether the settings have been already loaded for the first time in order to maintain a single
     * attachment of {@link UserStatusEventHandler}.
     */
    private boolean initialUserSetting = false;

    /**
     * Handler which is currently attached for listening the user's login state
     */
    private UserStatusEventHandler userStatusEventHandler = null;

    /**
     * Callbacks which are waiting to be called when persistent settings representation has loaded.
     */
    private Queue<OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>>> retrieveSettingsCallbacksQueue = new LinkedList<>();

    /**
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinitionId
     *            The definition for User Settings and Document Settings storage keys
     */
    public UserSettingsStorageManager(UserService userService, StorageDefinition storageDefinitionId) {
        this.userService = userService;
        this.storageGlobalKey = storageDefinitionId.generateStorageGlobalKey();
        this.storageContextSpecificKey = storageDefinitionId.generateStorageContextSpecificKey();
    }

    @Override
    public void storeSettingsRepresentation(PersistableSettingsRepresentations<String> settingsStrings,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        storeSettingsStringsOnLocalStorage(settingsStrings);
        if (userService.getCurrentUser() != null) {
            storeSettingsStringsOnServer(settingsStrings, onSettingsStoredCallback);
        } else {
            onSettingsStoredCallback.onSuccess();
        }
    }

    private void storeSettingsStringsOnLocalStorage(PersistableSettingsRepresentations<String> settingsStrings) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            if (settingsStrings.getGlobalSettingsRepresentation() != null) {
                localStorage.removeItem(storageGlobalKey);
                localStorage.setItem(storageGlobalKey, settingsStrings.getGlobalSettingsRepresentation());
            }
            if (settingsStrings.getContextSpecificSettingsRepresentation() != null) {
                localStorage.removeItem(storageContextSpecificKey);
                localStorage.setItem(storageContextSpecificKey,
                        settingsStrings.getContextSpecificSettingsRepresentation());
            }
        }
    }

    private void storeSettingsStringsOnServer(PersistableSettingsRepresentations<String> settingsStrings,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
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

        if (settingsStrings.getContextSpecificSettingsRepresentation() == null
                || settingsStrings.getGlobalSettingsRepresentation() == null) {
            if (settingsStrings.getContextSpecificSettingsRepresentation() != null) {
                userService.setPreference(storageContextSpecificKey,
                        settingsStrings.getContextSpecificSettingsRepresentation(), asyncCallback);
            }
            if (settingsStrings.getGlobalSettingsRepresentation() != null) {
                userService.setPreference(storageGlobalKey, settingsStrings.getGlobalSettingsRepresentation(),
                        asyncCallback);
            }
        } else {
            Map<String, String> keyValuePairs = new HashMap<>();
            if (settingsStrings.getGlobalSettingsRepresentation() != null) {
                keyValuePairs.put(storageGlobalKey, settingsStrings.getGlobalSettingsRepresentation());
            }
            if (settingsStrings.getContextSpecificSettingsRepresentation() != null) {
                keyValuePairs.put(storageContextSpecificKey,
                        settingsStrings.getContextSpecificSettingsRepresentation());
            }
            userService.setPreferences(keyValuePairs, asyncCallback);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (userStatusEventHandler != null) {
            userService.removeUserStatusEventHandler(userStatusEventHandler);
        }
    }

    private PersistableSettingsRepresentations<String> retrieveSettingsStringsFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String globalSettings = localStorage.getItem(storageGlobalKey);
            String contextSpecificSettings = localStorage.getItem(storageContextSpecificKey);
            return new PersistableSettingsRepresentations<String>(globalSettings, contextSpecificSettings);
        }
        return new PersistableSettingsRepresentations<String>(null, null);
    }

    private void retrieveSettingsStringsFromServer(
            final AsyncCallback<PersistableSettingsRepresentations<String>> asyncCallback) {
        List<String> keys = new ArrayList<>(2);
        keys.add(storageGlobalKey);
        keys.add(storageContextSpecificKey);
        userService.getPreferences(keys, new AsyncCallback<Map<String, String>>() {

            @Override
            public void onSuccess(Map<String, String> result) {
                PersistableSettingsRepresentations<String> settingsStrings = new PersistableSettingsRepresentations<>(
                        result.get(storageGlobalKey), result.get(storageContextSpecificKey));
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
    public void retrieveSettingsRepresentation(
            OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>> callback) {
        retrieveSettingsCallbacksQueue.add(callback);
        if (userStatusEventHandler == null) {
            this.userStatusEventHandler = new UserStatusEventHandler() {

                @Override
                public void onUserStatusChange(UserDTO user) {
                    // call this method always on user status change event in order to cause sync between local storage
                    // and server
                    if (!initialUserSetting) {
                        initialUserSetting = true;
                        retrieveSettingsStringsFromServerOrLocalStorage();
                    }
                }
            };
            userService.addUserStatusEventHandler(userStatusEventHandler, true);
        } else if (initialUserSetting) {
            retrieveSettingsStringsFromServerOrLocalStorage();
        }
    }

    private void retrieveSettingsStringsFromServerOrLocalStorage() {
        if (userService.getCurrentUser() == null) {
            PersistableSettingsRepresentations<String> settingsStrings = retrieveSettingsStringsFromLocalStorage();
            OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>> callback;
            while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                callback.onSuccess(settingsStrings);
            }
        } else {
            retrieveSettingsStringsFromServer(new AsyncCallback<PersistableSettingsRepresentations<String>>() {

                @Override
                public void onSuccess(PersistableSettingsRepresentations<String> serverSettingsStrings) {
                    serverSettingsStrings = syncLocalStorageAndServer(serverSettingsStrings);
                    OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>> callback;
                    while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                        callback.onSuccess(serverSettingsStrings);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    PersistableSettingsRepresentations<String> fallbackSettingsStrings = retrieveSettingsStringsFromLocalStorage();
                    OnSettingsLoadedCallback<PersistableSettingsRepresentations<String>> callback;
                    while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                        callback.onError(caught, fallbackSettingsStrings);
                    }
                }
            });
        }
    }

    private PersistableSettingsRepresentations<String> syncLocalStorageAndServer(
            PersistableSettingsRepresentations<String> serverSettingsStrings) {
        if (serverSettingsStrings.getGlobalSettingsRepresentation() == null
                && serverSettingsStrings.getContextSpecificSettingsRepresentation() == null) {
            PersistableSettingsRepresentations<String> localStorageSettingsStrings = retrieveSettingsStringsFromLocalStorage();
            if (localStorageSettingsStrings.getGlobalSettingsRepresentation() != null
                    || localStorageSettingsStrings.getContextSpecificSettingsRepresentation() != null) {
                storeSettingsStringsOnServer(localStorageSettingsStrings, new OnSettingsStoredCallback() {

                    @Override
                    public void onSuccess() {
                        // nothing to do
                    }

                    @Override
                    public void onError(Throwable caught) {
                        // nothing to do
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
