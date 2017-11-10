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
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;
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
public class UserSettingsStorageManager implements SettingsStorageManager {

    /**
     * The key which is associated with the User Settings. Different keys will cause multiple/different settings
     * instances to be stored in the storage.
     */
    private final String storageKeyForUserSettings;

    /**
     * The key which is associated with the Document Settings. Different keys will cause multiple/different settings
     * instances to be stored in the storage.
     */
    private final String storageKeyForDocumentSettings;

    /**
     * The userService instance which is used for server communication regarding settings loading and storing
     */
    private UserService userService;

    /**
     * Indicates whether the settings have been already loaded for the first time in order to maintain a single
     * attachment of {@link UserStatusEventHandler}.
     */
    private boolean initialized = false;

    /**
     * Indicates whether a user has been already logged in during his stay on the current page
     */
    private boolean userWasAlreadyLoggedIn = false;

    /**
     * Handler which is currently attached for listening the user's login state
     */
    private UserStatusEventHandler userStatusEventHandler = null;

    /**
     * Callbacks which are waiting to be called when persistent settings representation has loaded.
     */
    private Queue<OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings>> retrieveSettingsCallbacksQueue = new LinkedList<>();

    /**
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinitionId
     *            The definition for User Settings and Document Settings storage keys
     */
    public UserSettingsStorageManager(UserService userService, StoredSettingsLocation storageDefinitionId) {
        this.userService = userService;
        this.storageKeyForUserSettings = storageDefinitionId.generateStorageKeyForUserSettings();
        this.storageKeyForDocumentSettings = storageDefinitionId.generateStorageKeyForDocumentSettings();
    }

    @Override
    public void storeSettingsRepresentations(StorableRepresentationOfDocumentAndUserSettings settingsRepresentations,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        storeSettingsRepresentationsOnLocalStorage(settingsRepresentations);
        if (userService.getCurrentUser() != null) {
            storeSettingsRepresentationsOnServer(settingsRepresentations, onSettingsStoredCallback);
        } else {
            onSettingsStoredCallback.onSuccess();
        }
    }

    private void storeSettingsRepresentationsOnLocalStorage(
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentations) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            if (settingsRepresentations.hasStoredUserSettings()) {
                localStorage.removeItem(storageKeyForUserSettings);
                final String settingsString = settingsRepresentations.getUserSettingsRepresentation().asString();
                if(settingsString != null) {
                    localStorage.setItem(storageKeyForUserSettings,
                            settingsString);
                }
            }
            if (settingsRepresentations.hasStoredDocumentSettings()) {
                localStorage.removeItem(storageKeyForDocumentSettings);
                final String settingsString = settingsRepresentations.getDocumentSettingsRepresentation().asString();
                if(settingsString != null) {
                    localStorage.setItem(storageKeyForDocumentSettings,
                            settingsString);
                }
            }
        }
    }

    private void storeSettingsRepresentationsOnServer(
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentations,
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

        if (!settingsRepresentations.hasStoredDocumentSettings() || !settingsRepresentations.hasStoredUserSettings()) {
            if (settingsRepresentations.hasStoredDocumentSettings()) {
                userService.setPreference(storageKeyForDocumentSettings,
                        settingsRepresentations.getDocumentSettingsRepresentation().asString(), asyncCallback);
            }
            if (settingsRepresentations.hasStoredUserSettings()) {
                userService.setPreference(storageKeyForUserSettings,
                        settingsRepresentations.getUserSettingsRepresentation().asString(), asyncCallback);
            }
        } else {
            Map<String, String> keyValuePairs = new HashMap<>();
            if (settingsRepresentations.hasStoredUserSettings()) {
                keyValuePairs.put(storageKeyForUserSettings,
                        settingsRepresentations.getUserSettingsRepresentation().asString());
            }
            if (settingsRepresentations.hasStoredDocumentSettings()) {
                keyValuePairs.put(storageKeyForDocumentSettings,
                        settingsRepresentations.getDocumentSettingsRepresentation().asString());
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

    private StorableRepresentationOfDocumentAndUserSettings convertStringsToSettingsRepresentations(String userSettings,
            String documentSettings) {
        StorableSettingsRepresentation userSettingsRepresentation = userSettings == null ? null
                : new StorableSettingsRepresentation(userSettings);
        StorableSettingsRepresentation documentSettingsRepresentation = documentSettings == null ? null
                : new StorableSettingsRepresentation(documentSettings);
        return new StorableRepresentationOfDocumentAndUserSettings(userSettingsRepresentation,
                documentSettingsRepresentation);
    }

    private StorableRepresentationOfDocumentAndUserSettings retrieveSettingsRepresentationsFromLocalStorage() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        String userSettings = null;
        String documentSettings = null;
        if (localStorage != null) {
            userSettings = localStorage.getItem(storageKeyForUserSettings);
            documentSettings = localStorage.getItem(storageKeyForDocumentSettings);
        }
        return convertStringsToSettingsRepresentations(userSettings, documentSettings);
    }

    private void retrieveSettingsRepresentationsFromServer(
            final AsyncCallback<StorableRepresentationOfDocumentAndUserSettings> asyncCallback) {
        List<String> keys = new ArrayList<>(2);
        keys.add(storageKeyForUserSettings);
        keys.add(storageKeyForDocumentSettings);
        userService.getPreferences(keys, new AsyncCallback<Map<String, String>>() {

            @Override
            public void onSuccess(Map<String, String> result) {
                String userSettings = result.get(storageKeyForUserSettings);
                String documentSettings = result.get(storageKeyForDocumentSettings);
                StorableRepresentationOfDocumentAndUserSettings settingsRepresentations = convertStringsToSettingsRepresentations(
                        userSettings, documentSettings);
                asyncCallback.onSuccess(settingsRepresentations);
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
            OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> callback) {
        retrieveSettingsCallbacksQueue.add(callback);
        if (userStatusEventHandler == null) {
            this.userStatusEventHandler = new UserStatusEventHandler() {

                // call this method always on user status change event in order to cause sync between local storage
                // and server
                @Override
                public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                    // trigger settings retrievement whether on the first userStatusEventHandler call, or when a user
                    // has been logged in for the first time in order to initial local storage and server
                    // synchronisation
                    if (!initialized || userService.getCurrentUser() != null && !userWasAlreadyLoggedIn) {
                        initialized = true;
                        retrieveSettingsRepresentationsFromServerOrLocalStorage();
                    }
                }
            };
            userService.addUserStatusEventHandler(userStatusEventHandler, true);
        } else if (initialized) {
            retrieveSettingsRepresentationsFromServerOrLocalStorage();
        }
    }

    private void retrieveSettingsRepresentationsFromServerOrLocalStorage() {
        if (userService.getCurrentUser() == null) {
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentations = retrieveSettingsRepresentationsFromLocalStorage();
            OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> callback;
            while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                callback.onSuccess(settingsRepresentations);
            }
        } else {
            userWasAlreadyLoggedIn = true;
            retrieveSettingsRepresentationsFromServer(
                    new AsyncCallback<StorableRepresentationOfDocumentAndUserSettings>() {

                        @Override
                        public void onSuccess(
                                StorableRepresentationOfDocumentAndUserSettings serverSettingsRepresentations) {
                            serverSettingsRepresentations = syncLocalStorageAndServer(serverSettingsRepresentations);
                            OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> callback;
                            while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                                callback.onSuccess(serverSettingsRepresentations);
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            StorableRepresentationOfDocumentAndUserSettings fallbackSettingsRepresentations = retrieveSettingsRepresentationsFromLocalStorage();
                            OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> callback;
                            while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
                                callback.onError(caught, fallbackSettingsRepresentations);
                            }
                        }
                    });
        }
    }

    private StorableRepresentationOfDocumentAndUserSettings syncLocalStorageAndServer(
            StorableRepresentationOfDocumentAndUserSettings serverSettingsRepresentations) {
        if (!serverSettingsRepresentations.hasStoredUserSettings()
                && !serverSettingsRepresentations.hasStoredDocumentSettings()) {
            StorableRepresentationOfDocumentAndUserSettings localStorageSettingsRepresentations = retrieveSettingsRepresentationsFromLocalStorage();
            if (localStorageSettingsRepresentations.hasStoredUserSettings()
                    || localStorageSettingsRepresentations.hasStoredDocumentSettings()) {
                storeSettingsRepresentationsOnServer(localStorageSettingsRepresentations,
                        new OnSettingsStoredCallback() {

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
            serverSettingsRepresentations = localStorageSettingsRepresentations;
        } else {
            storeSettingsRepresentationsOnLocalStorage(serverSettingsRepresentations);
        }
        return serverSettingsRepresentations;
    }

}
