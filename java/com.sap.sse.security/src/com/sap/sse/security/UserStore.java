package com.sap.sse.security;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Named;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Keeps track of all {@link User}, {@link UserGroup} and {@link Role}
 * objects persistently; furthermore, aspects such as user access tokens, preferences and
 * settings are stored durably.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface UserStore extends UserGroupProvider, Named {
    /**
     * An instance of the bundle hosting this service may have a default tenant. If so, the default tenant's name is
     * read from a system property whose name is provided by this constant.
     */
    String DEFAULT_TENANT_NAME_PROPERTY_NAME = "security.defaultTenantName";


    Iterable<UserGroup> getUserGroups();
    
    UserGroup getUserGroup(UUID groupId);
    
    UserGroup getUserGroupByName(String name);
    
    UserGroup createUserGroup(UUID groupId, String name) throws UserGroupManagementException;
    
    void updateUserGroup(UserGroup userGroup);
    
    void deleteUserGroup(UserGroup userGroup) throws UserGroupManagementException;
    
    Iterable<User> getUsers();
    
    boolean hasUsers();

    /**
     * The user with that {@link UserImpl#getName() name} or {@code null} if no such user exists
     */
    User getUserByName(String username);

    /**
     * The user with that {@link UserImpl#getEmail() email} or {@code null} if no such user exists
     */
    SecurityUser getUserByEmail(String email);
    
    User getUserByAccessToken(String accessToken);

    UserImpl createUser(String name, String email, UserGroup defaultTenant, Account... accounts) throws UserManagementException;

    void updateUser(User user);

    Iterable<Role> getRolesFromUser(String username) throws UserManagementException;

    void addRoleForUser(String username, Role role) throws UserManagementException;

    void removeRoleFromUser(String username, Role role) throws UserManagementException;

    Iterable<WildcardPermission> getPermissionsFromUser(String username) throws UserManagementException;

    void removePermissionFromUser(String username, WildcardPermission permission) throws UserManagementException;

    void addPermissionForUser(String username, WildcardPermission permission) throws UserManagementException;

    void deleteUser(String username) throws UserManagementException;

    Iterable<RoleDefinition> getRoleDefinitions();
    RoleDefinition getRoleDefinition(UUID roleDefinitionId);
    RoleDefinition createRoleDefinition(UUID roleDefinitionId, String displayName, Iterable<WildcardPermission> permissions);
    void setRoleDefinitionPermissions(UUID roleDefinitionId, Set<WildcardPermission> permissions);
    void addRoleDefinitionPermission(UUID roleDefinitionId, WildcardPermission permission);
    void removeRoleDefinitionPermission(UUID roleDefinitionId, WildcardPermission permission);
    void setRoleDefinitionDisplayName(UUID roleDefinitionId, String displayName);
    void removeRoleDefinition(RoleDefinition roleDefinition);

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
     * <p>
     * In an OSGi environment, this shouldn't be called manually, but instead automatically managed by setting a
     * {@link PreferenceConverterRegistrationManager} up. {@link PreferenceConverter}s should be registered in the OSGi
     * service registry with {@link PreferenceConverter#KEY_PARAMETER_NAME} containing the associated preference key
     * added as property of the service registration.
     * </p>
     * 
     * <p>
     * Registers a converter objects for a preference key that is used to convert preference Strings to Objects. This
     * makes it possible to access deserialized settings without the need to do the deserialization over and over again.
     * </p>
     * 
     * @param key
     *            the key to associate the converter with
     * @param converter
     *            the converter to use for (de)serialization
     */
    void registerPreferenceConverter(String key, PreferenceConverter<?> converter);
    
    /**
     * <p>
     * In an OSGi environment, this shouldn't be called manually, but instead automatically managed by setting a
     * {@link PreferenceConverterRegistrationManager} up.
     * </p>
     * 
     * <p>
     * Removes a registered {@link PreferenceConverter}  with the given key.
     * </p>
     * 
     * @param key
     *            the key of the {@link PreferenceConverter} to remove
     */
    void removePreferenceConverter(String key);
    
    /**
     * Gets a preference object. Always returns null if there is no converter associated with the given key -> see
     * {@link #registerPreferenceConverter(String, PreferenceConverter)}.
     */
    <T> T getPreferenceObject(String username, String key);
    
    /**
     * Sets a preference as Object. This converts the given Object to a preference {@link String} using a
     * {@link PreferenceConverter} that was registered through
     * {@link #registerPreferenceConverter(String, PreferenceConverter)}.
     * 
     * @return the {@link String}-converted value of the preference object, as internally passed to
     *         {@link #setPreference(String, String, String)}
     * 
     * @throws IllegalArgumentException
     *             if there is no {@link PreferenceConverter} registered with the given key.
     */
    String setPreferenceObject(String username, String key, Object preferenceObject) throws IllegalArgumentException;

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
     * Registered listeners will not be removed automatically.
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

    void removeAccessToken(String username);

    /**
     * The owner and any subject having the {@link DefaultRoles#ADMIN} role can retrieve an existing
     * authentication token for the user. {@code null} may result in case for the user identified by
     * {@code username} no access token has previously been {@link #setAccessToken(String, String) set}.
     */
    String getAccessToken(String username);
    
    void addPreferenceObjectListener(String key, PreferenceObjectListener<?> listener, boolean fireForAlreadyExistingPreferences);
    
    void removePreferenceObjectListener(PreferenceObjectListener<?> listener);

    /**
     * If a valid default tenant name was passed to the constructor, this field will contain a valid
     * {@link UserGroup} object whose name equals that of the default tenant name. It will have been used
     * during role migration where string-based roles are mapped to a corresponding {@link RoleDefinition}
     * and the users with the original role will obtain a corresponding {@link Role} with this default
     * tenant as the {@link Role#getQualifiedForTenant() tenant qualifier}.
     */
    UserGroup getDefaultTenant();

    /**
     * Ensures that the predefined role definitions, particularly the "admin" and the "user" role, exist.
     */
    void ensureDefaultRolesExist();

    /**
     * @return a pair with: <br/>
     *         If A is true, at least one user has an unqualified version of the {@link #roleToCheck} (without tenant or
     *         user qualification). In this case, B is null.<br/>
     *         If A is false, B contains all the ownerships of {@link #roleToCheck}
     */
    Pair<Boolean, Set<Ownership>> getExistingQualificationsForRoleDefinition(RoleDefinition roleToCheck);

    Set<Pair<User, Role>> getRolesQualifiedByUserGroup(UserGroup groupQualification);
}
