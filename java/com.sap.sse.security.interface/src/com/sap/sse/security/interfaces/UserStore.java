package com.sap.sse.security.interfaces;

import com.sap.sse.security.shared.BasicUserStore;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
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
     * An instance of the bundle hosting this service may have a default tenant. If so, the default tenant's name is
     * read from a system property whose name is provided by this constant.
     */
    String DEFAULT_TENANT_NAME_PROPERTY_NAME = "security.defaultTenantName";
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
    void loadAndMigrateUsers() throws UserGroupManagementException, UserManagementException;

    void ensureDefaultTenantExists() throws UserGroupManagementException;
}
