package com.sap.sse.security.ui.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

/**
 * Encapsulates the current user, remembered as a {@link UserDTO} object. The current user is determined by a call to
 * the server which in turn considers the current session to determine if a valid user is attached to the session. The
 * {@link UserDTO} for the current user tells about the user's roles, the user name and, if known, the e-mail address.
 * <p>
 * 
 * The {@link UserDTO} object is {@link #updateUser(boolean) updated} once the {@link #login(String, String)} or
 * {@link #logout()} methods have completed, and registered handlers and, optionally, other instances of this
 * service living in other tabs or browser windows, are notified about the changes.<p>
 * 
 * Clients can subscribe to this service for changes of the current user, using
 * {@link #addUserStatusEventHandler(UserStatusEventHandler)}. They will be notified each time the user object is
 * fetched successfully from the server or when a log-out sets the user object to <code>null</code>.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);
    
    /**
     * Key for the HTML5 local store that can be used to notify other instances of this service in other tabs
     * and windows about changes in the currently logged-in user. 
     */
    private static final String LOCAL_STORAGE_UPDATE_KEY = "current-user-has-changed";

    private final UserManagementServiceAsync userManagementService;

    private final Storage localStorage;

    private final Set<UserStatusEventHandler> handlers;

    private UserDTO currentUser;

    private final UUID id;

    public UserService(UserManagementServiceAsync userManagementService) {
        this.id = UUID.randomUUID();
        this.userManagementService = userManagementService;
        localStorage = Storage.getLocalStorageIfSupported();
        handlers = new HashSet<>();
        registerStorageListener();
        updateUser(/* notifyOtherInstances */ false);
    }
    
    /**
     * Used to synchronize changes in the user status between all {@link UserService} instances across all browser
     * tabs/windows.
     */
    public void fireUserUpdateEvent() {
        if (localStorage != null) {
            localStorage.setItem(LOCAL_STORAGE_UPDATE_KEY, id.toString());
            localStorage.setItem(LOCAL_STORAGE_UPDATE_KEY, "");
        }
    }

    /**
     * Lets this user service get notified if other tabs or browser windows update the logged-in user
     */
    private void registerStorageListener() {
        if (localStorage != null) {
            Storage.addStorageEventHandler(new StorageEvent.Handler() {
                @Override
                public void onStorageChange(StorageEvent event) {
                    // ignore update events coming from this object itself
                    if (LOCAL_STORAGE_UPDATE_KEY.equals(event.getKey()) && event.getNewValue() != null
                            && !event.getNewValue().isEmpty() && !event.getNewValue().equals(id.toString())) {
                        updateUser(/* Don't play endless ping-ping between instances! */ false);
                    }
                }
            });
        }
    };
    
    /**
     * Fetches the {@link UserDTO} for the currently signed-in user, identified by the current session, from the server.
     * Receiving a result or an error condition will {@link #fireUserUpdateEvent() fire an update event} to all other
     * instances of this class in other tabs / windows if <code>notifyOtherInstances</code> is <code>true</code>. All
     * {@link UserStatusEventHandler}s registered with this instance will be
     * {@link UserStatusEventHandler#onUserStatusChange(UserDTO) notified} in all cases.
     * 
     * @param notifyOtherInstances
     *            if <code>true</code>, other instances of this class will be notified about the result of the call
     */
    private void updateUser(final boolean notifyOtherInstances) {
        userManagementService.getCurrentUser(new MarkedAsyncCallback<UserDTO>(
                new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                setCurrentUser(result, notifyOtherInstances);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        }));
    }
    
    /**
     * Signs in a user with username and password. If successful, the {@link #getCurrentUser() current user} will be
     * updated with the user data. Otherwise, it will remain unchanged. This means in particular that any previously
     * signed-in user will remain to be signed in.
     */
    public void login(String username, String password, final AsyncCallback<SuccessInfo> callback) {
        userManagementService.login(username, password,
                new MarkedAsyncCallback<SuccessInfo>(new AsyncCallback<SuccessInfo>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(SuccessInfo result) {
                if (result.isSuccessful()) {
                    setCurrentUser(result.getUserDTO(), /* notify other instances */ true);
                }
                callback.onSuccess(result);
            }
        }));
    }
    
    public void verifySocialUser(final AsyncCallback<UserDTO> callback) throws Exception {
        final String authProviderName = ClientUtils.getAuthProviderNameFromCookie();
        logger.info("Verifying " + authProviderName + " user ...");
        userManagementService.verifySocialUser(ClientUtils.getCredential(),
                new MarkedAsyncCallback<UserDTO>(new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(UserDTO result) {
                setCurrentUser(result, /* notifyOtherInstances */ true);
                logger.info(authProviderName + " user '" + result.getName() + "' is verified!\n");
                callback.onSuccess(result);
            }
        }));
    }

    public void logout() {
        userManagementService.logout(new AsyncCallback<SuccessInfo>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotSignOut(caught.getMessage()));
            }
    
            @Override
            public void onSuccess(SuccessInfo result) {
                currentUser = null;
                notifyUserStatusEventHandlers();
                fireUserUpdateEvent();
            }
        });
    };

    /**
     * The user currently signed in, identified using the current web session, or <code>null</code> if no user is
     * currently signed in.
     */
    public UserDTO getCurrentUser() {
        return currentUser;
    }
    
    private void setCurrentUser(UserDTO result, final boolean notifyOtherInstances) {
        currentUser = result;
        logger.info("User changed to " + (result == null ? "No User" : (result.getName() + " roles: "
                + result.getRoles())));
        notifyUserStatusEventHandlers();
        if (notifyOtherInstances) {
            fireUserUpdateEvent();
        }
    }

    public void addUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.add(handler);
    }

    public void removeUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.remove(handler);
    }

    private void notifyUserStatusEventHandlers() {
        for (UserStatusEventHandler handler : handlers) {
            handler.onUserStatusChange(getCurrentUser());
        }
    }
    
    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }
}
