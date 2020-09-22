package com.sap.sse.security.interfaces;

import com.sap.sse.security.shared.BasicUserStore;
import com.sap.sse.security.shared.UserStoreManagementException;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.shared.impl.UserGroupImpl;

/**
 * Keeps track of all {@link User}, {@link UserGroupImpl} and {@link Role}
 * objects persistently; furthermore, aspects such as user access tokens, preferences and
 * settings are stored durably.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface UserStore extends BasicUserStore {
    /**
     * An instance of the bundle hosting this service may have a server group. If so, the default server group's name is
     * read from a system property whose name is provided by this constant.
     */
    String DEFAULT_SERVER_GROUP_NAME_PROPERTY_NAME = "security.defaultServerGroupName";
    String ADMIN_USERNAME = "admin";

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
     * Replaces all existing contents by those provided by the <code>newUserStore</code>. This has no impact on the persistent
     * representation of this store and is meant for use on a replica only; the replica's database state is undefined.
     */
    void replaceContentsFrom(UserStore newUserStore);
    
    void addPreferenceObjectListener(String key, PreferenceObjectListener<?> listener, boolean fireForAlreadyExistingPreferences);
    
    void removePreferenceObjectListener(PreferenceObjectListener<?> listener);

    /**
     * Do not call this before the RolePrototypes are created/loaded, as else a migration cannot succeed. But do call
     * this before the SecurityService is created, as else new defaults (eg admin user) will be created
     */
    void loadAndMigrateUsers() throws UserStoreManagementException;

    /**
     * Looks up a {@link UserGroup} based on the server group name set for this user store. If no such group
     * can be found, one is created and returned. If a group by that name exists, it will be set as this {@link UserStore}'s
     * {@link #getServerGroup() server group} and will be returned.
     */
    UserGroup ensureServerGroupExists() throws UserGroupManagementException;

    void removeAllQualifiedRolesForUser(User user);

    RoleDefinition getRoleDefinitionByPrototype(RolePrototype rolePrototype);

    void deleteUserGroupAndRemoveAllQualifiedRolesForUserGroup(UserGroup userGroup) throws UserGroupManagementException;

    void setDefaultTennantForUserAndUpdate(User user, UserGroup newDefaultTenant, String serverName);
}
