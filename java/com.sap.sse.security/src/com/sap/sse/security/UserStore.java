package com.sap.sse.security;

import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.UserManagementException;

public interface UserStore extends Named {
    Iterable<User> getUsers();

    User getUserByName(String name);

    User getUserByEmail(String email);
    
    User getUserByAccessToken(String accessToken);

    User createUser(String name, String email, Account... accounts) throws UserManagementException;

    void updateUser(User user);

    Iterable<String> getRolesFromUser(String username) throws UserManagementException;

    void addRoleForUser(String name, String role) throws UserManagementException;

    void removeRoleFromUser(String name, String role) throws UserManagementException;

    Iterable<String> getPermissionsFromUser(String username) throws UserManagementException;

    void removePermissionFromUser(String name, String permission) throws UserManagementException;

    void addPermissionForUser(String name, String permission) throws UserManagementException;

    void deleteUser(String name) throws UserManagementException;

    /**
     * Registers a settings key together with its type. Calling this method is necessary for {@link #setSetting(String, Object)}
     * to have an effect for <code>key</code>. Calls to {@link #setSetting(String, Object)} will only accept values whose type
     * is compatible with <code>type</code>. Note that the store implementation may impose constraints on the types supported.
     * All store implementations are required to support at least {@link String} and {@link UUID} as types.
     */
    void addSetting(String key, Class<?> type);
    
    void setPreference(String username, String key, String value);
    
    /**
     * Always returns a valid map which may be empty.
     */
    Map<String, String> getAllPreferences(String username);
    
    void unsetPreference(String username, String key);

    String getPreference(String username, String key);

    /**
     * Sets a value for a key if that key was previously added to this store using {@link #addSetting(String, Class)}.
     * For user store implementations that maintain their data persistently and make it available after a server
     * restart, it is sufficient to register the settings key once because these registrations will be stored
     * persistently, too.
     * <p>
     * 
     * If the <code>key</code> was not registered before by a call to {@link #addSetting(String, Class)}, or if the
     * <code>setting</code> object does not conform with the type passed to {@link #addSetting(String, Class)}, a call
     * to this method will have no effect and return <code>false</code>.
     * 
     * @Return whether applying the setting was successful; <code>false</code> means that no update was performed to the
     * setting because either the key was not registered before by {@link #addSetting(String, Class)} or the type of the
     * <code>setting</code> object does not conform to the type used in {@link #addSetting(String, Class)}
     */
    boolean setSetting(String key, Object setting);

    <T> T getSetting(String key, Class<T> clazz);

    Map<String, Object> getAllSettings();
    
    Map<String, Class<?>> getAllSettingTypes();

    /**
     * Removes all users and all their preferences and all settings from this store's in-memory representation.
     * For safety reasons and because a replica's DB state is undefined anyhow, leaves persistent content in place.
     * Use with due care.
     */
    void clear();

    /**
     * Replaces all existing contents by those provided by the <code>newUserStore</code>. This has no impact on the persistent
     * representation of this store and is meant for use on a replica only; the replica's database state is undefined.
     */
    void replaceContentsFrom(UserStore newUserStore);

    /**
     * Stores an access token that can be used to authenticate the user identified by <code>username</code>.
     * If there is no user by that name, calling this method has no effect and it will return <code>false</code>.
     * 
     * @return whether a user could be identified by <code>username</code>
     */
    boolean setAccessToken(String username, String accessToken);

}
