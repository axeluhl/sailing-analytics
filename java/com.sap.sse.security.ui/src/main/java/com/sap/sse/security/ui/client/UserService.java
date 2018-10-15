package com.sap.sse.security.ui.client;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.StorageEvent;
import com.sap.sse.gwt.client.StorageEvent.Handler;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

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
    
    
    /**
     * Storage key to remember when a user was authenticated or dismissed the login hint the last time.
     */
    protected static final String STORAGE_KEY_FOR_USER_LOGIN_HINT = "sse.ui.lastLoginOrSuppression";
    
    /**
     * Delay when the login hint will be shown next time after a user logged in or dismissed the message.
     */
    protected static final Duration SUPRESSION_DELAY = Duration.ONE_WEEK;
    
    private final UserManagementServiceAsync userManagementService;

    private final Set<UserStatusEventHandler> handlers;

    private boolean userInitiallyLoaded = false;
    
    private boolean preAuthenticated = false;
    
    private UserDTO currentUser;

    private final String id;

    private UserDTO anonymousUser;

    public UserService(UserManagementServiceAsync userManagementService) {
        this.id = ""+(System.currentTimeMillis() * Random.nextInt()); // something pretty random
        this.userManagementService = userManagementService;
        handlers = new HashSet<>();
        registerStorageEventHandler();
        updateUser(/* notifyOtherInstances */ false);
    }

    private void registerStorageEventHandler() {
        Storage.addStorageEventHandler(new Handler() {
            @Override
            public void onStorageChange(StorageEvent event) {
                logger.finest("Received storage event { key: "+event.getKey()+", newValue: "+event.getNewValue()+", oldValue: "+
                        event.getOldValue()+", url: "+event.getUrl()+", storageArea: "+event.getStorageArea());
                // ignore update events coming from this object itself
                if (LOCAL_STORAGE_UPDATE_KEY.equals(event.getKey()) && event.getNewValue() != null
                        && !event.getNewValue().isEmpty() && !event.getNewValue().equals(id.toString())) {
                    updateUser(/* Don't play endless ping-ping between instances! */ false);
                }
            }
        });
    }

    /**
     * Used to synchronize changes in the user status between all {@link UserService} instances across all browser
     * tabs/windows.
     */
    public void fireUserUpdateEvent() {
        if (Storage.isSupported()) {
            Storage.getLocalStorageIfSupported().setItem(LOCAL_STORAGE_UPDATE_KEY, ""); // force a change
            Storage.getLocalStorageIfSupported().setItem(LOCAL_STORAGE_UPDATE_KEY, id);
        }
    }

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
    public void updateUser(final boolean notifyOtherInstances) {
        userManagementService.getCurrentUser(
                new MarkedAsyncCallback<Pair<UserDTO, UserDTO>>(new AsyncCallback<Pair<UserDTO, UserDTO>>() {
            @Override
                    public void onSuccess(Pair<UserDTO, UserDTO> result) {
                setCurrentUser(result, notifyOtherInstances);
            }

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(caught.getMessage(), NotificationType.ERROR);
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
                new MarkedAsyncCallback<Pair<UserDTO, UserDTO>>(new AsyncCallback<Pair<UserDTO, UserDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
                    public void onSuccess(Pair<UserDTO, UserDTO> result) {
                setCurrentUser(result, /* notifyOtherInstances */ true);
                        logger.info(authProviderName + " user '" + result.getA().getName() + "' is verified!\n");
                        callback.onSuccess(result.getA());
            }
        }));
    }

    public void logout() {
        userManagementService.logout(new AsyncCallback<SuccessInfo>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.couldNotSignOut(caught.getMessage()), NotificationType.ERROR);
            }
    
            @Override
            public void onSuccess(SuccessInfo result) {
                currentUser = null;
                preAuthenticated = false;
                notifyUserStatusEventHandlers(false);
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

    private void setCurrentUser(Pair<UserDTO, UserDTO> resultAndAnomynous, final boolean notifyOtherInstances) {
        if (resultAndAnomynous.getA() == null) {
            currentUser = null;
        } else {
            // we remember that a user was authenticated to suppress the hint for some time
            setUserLoginHintToStorage();
            currentUser = resultAndAnomynous.getA();
        }
        anonymousUser = resultAndAnomynous.getB();

        preAuthenticated = (!userInitiallyLoaded && currentUser != null);
        userInitiallyLoaded = true;
        logger.info("User changed to "
                + (currentUser == null ? "No User" : (currentUser.getName() + " roles: " + currentUser.getRoles())));
        logger.info("User anonymous changed to " + anonymousUser.getName() + " roles: " + anonymousUser.getRoles());
        notifyUserStatusEventHandlers(preAuthenticated);
        if (notifyOtherInstances) {
            fireUserUpdateEvent();
        }
    }

    public void addUserStatusEventHandler(UserStatusEventHandler handler) {
        addUserStatusEventHandler(handler, false);
    }
    
    public void addUserStatusEventHandler(UserStatusEventHandler handler, boolean fireIfUserIsAlreadyAvailable) {
        handlers.add(handler);
        if (userInitiallyLoaded && fireIfUserIsAlreadyAvailable) {
            handler.onUserStatusChange(currentUser, preAuthenticated);
        }
    }

    public void removeUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.remove(handler);
    }

    private void notifyUserStatusEventHandlers(boolean preAuthenticated) {
        for (UserStatusEventHandler handler : new HashSet<>(handlers)) {
            handler.onUserStatusChange(getCurrentUser(), preAuthenticated);
        }
    }

    public UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }
    
    /**
     * Loads the {@link #getCurrentUser() current user}'s preference with the given {@link String key} from server.
     * The preferences are passed to the {@link AsyncCallback} as serialized in {@link String}.
     * 
     * @param key
     *            key of the preference to load
     * @param callback
     *            {@link AsyncCallback} for GWT RPC call
     *            
     */
    public void getPreference(String key,
            final AsyncCallback<String> callback) {
        String username = getCurrentUser().getName(); // TODO: Can username be determined via session on server-side
        getUserManagementService().getPreference(username, key, callback);
    }
    
    public void getPreferences(List<String> keys,
            final AsyncCallback<Map<String, String>> callback) {
        String username = getCurrentUser().getName(); // TODO: Can username be determined via session on server-side
        getUserManagementService().getPreferences(username, keys, callback);
    }
    
    public void getAllPreferences(final AsyncCallback<Map<String, String>> callback) {
        String username = getCurrentUser().getName(); // TODO: Can username be determined via session on server-side
        getUserManagementService().getAllPreferences(username, callback);
    }
    
    /**
     * Sets the {@link #getCurrentUser() current user}'s preference with the given {@link String key} on server.
     * 
     * @param key
     *            key of the preference to set
     * @param serializedSettings
     *            Serialized settings as {@link String} containing the preferences
     */
    public void setPreference(String key, String serializedSettings, final AsyncCallback<Void> callback) {
        String username = getCurrentUser().getName(); // TODO: Can username be determined via session on server-side
        getUserManagementService().setPreference(username, key, serializedSettings, callback);
    }
    
    public void setPreferences(Map<String, String> keyValuePairs,
            final AsyncCallback<Void> callback) {
        String username = getCurrentUser().getName(); // TODO: Can username be determined via session on server-side
        getUserManagementService().setPreferences(username, keyValuePairs, callback);
    }
    
    /**
     * Unsets the {@link #getCurrentUser() current user}'s preference with the given key on server.
     * 
     * @param key key of the preference to unset
     *            
     * @see GenericSerializableSettings
     * @see AbstractGenericSerializableSettings
     */
    public void unsetPreference(String key) {
        String username = getCurrentUser().getName(); // TODO: Can username be determined via session on server-side
        getUserManagementService().unsetPreference(username, key, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO What to do in case of failure?
            }
            
            @Override
            public void onSuccess(Void result) {
                // TODO Do anything in case of success?
            }
        });
    }
    
    /**
     * Unauthenticated users get a hint that it has benefits to create an account and log in.When a user was recently
     * logged in or dismissed the notification, he won't see the hint again for some time. This method checks if a user
     * was logged in or dismissed the message recently.
     */
    public boolean wasUserRecentlyLoggedInOrDismissedTheHint() {
        final TimePoint lastLoginOrSupression = parseLastNewUserSupression();
        return lastLoginOrSupression != null
                && lastLoginOrSupression.plus(SUPRESSION_DELAY).after(MillisecondsTimePoint.now());
    }

    private TimePoint parseLastNewUserSupression() {
        TimePoint lastLoginOrSupression = null;
        final Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            final String stringValue = storage.getItem(STORAGE_KEY_FOR_USER_LOGIN_HINT);
            try {
                if (stringValue != null) {
                    lastLoginOrSupression = new MillisecondsTimePoint(Long.parseLong(stringValue));
                }
            } catch (Exception e) {
                logger.warning("Error parsing localstore value '" + stringValue + "'");
                storage.removeItem(STORAGE_KEY_FOR_USER_LOGIN_HINT);
            }
        }
        return lastLoginOrSupression;
    }

    /**
     * Unauthenticated users get a hint that it has benefits to create an account and log in. When a user was recently
     * logged in or dismissed the notification, he won't see the hint again for some time. This method triggers the
     * suppression.
     */
    public void setUserLoginHintToStorage() {
        final Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            storage.setItem(STORAGE_KEY_FOR_USER_LOGIN_HINT, String.valueOf(MillisecondsTimePoint.now().asMillis()));
        }
    }
    
    public boolean hasPermission(String permission) {
        return hasPermission(new WildcardPermission(permission));
    }

    public boolean hasPermission(WildcardPermission permission) {
        return hasPermission(permission, /* ownership */ null, /* acl */ null);
    }
    
    public boolean hasPermission(WildcardPermission permission, Ownership ownership) {
        return hasPermission(permission, ownership, /* acl */ null);
    }
    
    public boolean hasPermission(WildcardPermission permission, Ownership ownership, AccessControlList acl) {
        if (anonymousUser == null) {
            return false;
        }
        if (anonymousUser.hasPermission(permission, ownership, acl)) {
            return true;
        }
        return currentUser != null && currentUser.hasPermission(permission, ownership, acl);
    }

    /**
     * Checks whether the user has permission to {@link DefaultActions#CREATE create} an object of the logical type
     * specified, assuming that it will be created with this user as the {@link Ownership#getUserOwner() user owner} and
     * this user's {@link #getDefaultTenant() default group} as the {@link Ownership#getTenantOwner() group owner}.
     */
    public boolean hasCreatePermission(HasPermissions logicalSecuredObjectType) {
        if (currentUser == null) {
            return false;
        }
        return hasPermission(logicalSecuredObjectType.getPermission(DefaultActions.CREATE),
                new OwnershipImpl(currentUser, currentUser.getDefaultTenant()));
    }
    
    public String getCurrentTenantName() {
        if (currentUser == null) {
            return null;
        } else {
            final UserGroup defaultTenant = currentUser.getDefaultTenant();
            return defaultTenant == null ? null : defaultTenant.getName();
        }
    }
}
