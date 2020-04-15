package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;

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
        final CrossDomainStorage storage = userService.getStorage();
        if (settingsRepresentations.hasStoredUserSettings()) {
            final String settingsString = settingsRepresentations.getUserSettingsRepresentation().asString();
            if (settingsString != null) {
                storage.setItem(storageKeyForUserSettings, settingsString, /* callback */ null);
            } else {
                storage.removeItem(storageKeyForUserSettings, /* callback */ null);
            }
        }
        if (settingsRepresentations.hasStoredDocumentSettings()) {
            final String settingsString = settingsRepresentations.getDocumentSettingsRepresentation().asString();
            if (settingsString != null) {
                storage.setItem(storageKeyForDocumentSettings, settingsString, /* callback */ null);
            } else {
                storage.removeItem(storageKeyForDocumentSettings, /* callback */ null);
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

    private void retrieveSettingsRepresentationsFromLocalStorage(final Iterable<Consumer<StorableRepresentationOfDocumentAndUserSettings>> resultCallbacks) {
        CrossDomainStorage localStorage = userService.getStorage();
        localStorage.getItem(storageKeyForUserSettings, userSettings->
            localStorage.getItem(storageKeyForDocumentSettings, documentSettings->
                resultCallbacks.forEach(resultCallback->resultCallback.accept(convertStringsToSettingsRepresentations(userSettings, documentSettings)))));
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
            retrieveSettingsRepresentationsFromLocalStorage(getQueuedSettingsCallbacks());
        } else {
            userWasAlreadyLoggedIn = true;
            retrieveSettingsRepresentationsFromServer(
                    new AsyncCallback<StorableRepresentationOfDocumentAndUserSettings>() {
                        @Override
                        public void onSuccess(
                                StorableRepresentationOfDocumentAndUserSettings serverSettingsRepresentations) {
                            syncLocalStorageAndServer(serverSettingsRepresentations, getQueuedSettingsCallbacks());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            retrieveSettingsRepresentationsFromLocalStorage(getQueuedSettingsCallbacks());
                        }
                    });
        }
    }

    private List<Consumer<StorableRepresentationOfDocumentAndUserSettings>> getQueuedSettingsCallbacks() {
        OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> callback;
        final List<Consumer<StorableRepresentationOfDocumentAndUserSettings>> callbacks = new LinkedList<>();
        while ((callback = retrieveSettingsCallbacksQueue.poll()) != null) {
            final OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> finalCallback = callback;
            callbacks.add(settingsRepresentations->finalCallback.onSuccess(settingsRepresentations));
        }
        return callbacks;
    }

    private void syncLocalStorageAndServer(
            StorableRepresentationOfDocumentAndUserSettings serverSettingsRepresentations,
            Iterable<Consumer<StorableRepresentationOfDocumentAndUserSettings>> resultCallbacks) {
        if (!serverSettingsRepresentations.hasStoredUserSettings()
                && !serverSettingsRepresentations.hasStoredDocumentSettings()) {
            retrieveSettingsRepresentationsFromLocalStorage(Collections.singleton(localStorageSettingsRepresentations->{
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
                resultCallbacks.forEach(resultCallback->resultCallback.accept(localStorageSettingsRepresentations));
            }));
        } else {
            storeSettingsRepresentationsOnLocalStorage(serverSettingsRepresentations);
            resultCallbacks.forEach(resultCallback->resultCallback.accept(serverSettingsRepresentations));
        }
    }

}
