package com.sap.sse.security;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.osgi.framework.BundleContext;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.operations.SecurityOperation;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * A service interface for security management. Intended to be used as an OSGi service that can be registered, e.g., by
 * {@link BundleContext#registerService(Class, Object, java.util.Dictionary)} and can be discovered by other bundles.
 * 
 * @author Axel Uhl (D043530)
 * @author Benjamin Ebling
 *
 */
public interface SecurityService extends ReplicableWithObjectInputStream<ReplicableSecurityService, SecurityOperation<?>> {
    SecurityManager getSecurityManager();

    /**
     * Return the ownership information for the object identified by {@code idOfOwnedObject}. If there is no
     * ownership information for that object and there is a default tenant available, create a default {@link Ownership}
     * information that lists the default tenant as the tenant owner for the object in question; no user owner is
     * specified. If no default tenant is available and no ownership information for the object with the ID specified
     * is found, {@code null} is returned.
     */
    OwnershipAnnotation getOwnership(QualifiedObjectIdentifier idOfOwnedObject);
    
    OwnershipAnnotation createDefaultOwnershipForNewObject(QualifiedObjectIdentifier idOfNewObject);
    
    void setOwnership(OwnershipAnnotation ownership);

    void deleteAllDataForRemovedObject(QualifiedObjectIdentifier idOfRemovedObject);

    Iterable<AccessControlListAnnotation> getAccessControlLists();

    AccessControlListAnnotation getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject);

    /**
     * @param idOfAccessControlledObject Has to be globally unique
     */
    SecurityService setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject);

    /**
     * @param id Has to be globally unique
     */
    SecurityService setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String displayNameOfAccessControlledObject);

    AccessControlList updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, Map<UserGroup, Set<String>> permissionMap);

    /**
     * @param name The name of the user group to add
     */
    AccessControlList addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup, String action);

    /**
     * @param name The name of the user group to remove
     */
    AccessControlList removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup group, String action);

    void deleteAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject);

    /**
     * Same as {@link #setOwnership(String, UserImpl, Tenant, String)}, leaving the display name
     * of the object owned undefined.
     */
    Ownership setOwnership(QualifiedObjectIdentifier idOfOwnedObject, SecurityUser userOwner, UserGroup tenantOwner);

    /**
     * @param idOfOwnedObject
     *            the ID of the object for which ownership is declared
     * @param userOwner
     *            the user to become the owning user of the object with ID
     *            {@code idOfOwnedObject}
     * @param tenantOwner
     *            the tenant to become owning tenant of the object with ID {@code idOfOwnedObject}
     * @param displayNameOfOwnedObject
     *            a display name that this store can use to produce a user-readable hint regarding the ownership
     *            definition that this call creates; there is no guarantee that the display name will remain up to date
     *            as the object identified by {@link idOfOwnedObject} may change its name without notifying this
     *            store
     */
    Ownership setOwnership(QualifiedObjectIdentifier idOfOwnedObject, SecurityUser userOwner, UserGroup tenantOwner, String displayNameOfOwnedObject);

    void deleteOwnership(QualifiedObjectIdentifier idOfOwnedObject);

    Iterable<UserGroup> getUserGroupList();

    UserGroup getUserGroup(UUID id);

    UserGroup getUserGroupByName(String name);
    
    Iterable<UserGroup> getUserGroupsOfUser(SecurityUser user);

    UserGroup createUserGroup(UUID id, String name) throws UserGroupManagementException;

    void addUserToUserGroup(UserGroup group, SecurityUser user);
    
    void removeUserFromUserGroup(UserGroup group, SecurityUser user);

    void deleteUserGroup(UserGroup userGroup) throws UserGroupManagementException;

    Iterable<User> getUserList();

    User getUserByName(String username);

    SecurityUser getUserByEmail(String email);

    User getCurrentUser();

    /**
     * Returns the redirect URL
     */
    String login(String username, String password) throws UserManagementException;

    String getAuthenticationUrl(Credential credential) throws UserManagementException;

    User verifySocialUser(Credential credential) throws UserManagementException;

    void logout();

    /**
     * @param validationBaseURL if <code>null</code>, no validation will be attempted
     */
    SecurityUser createSimpleUser(String username, String email, String password, String fullName, String company, String validationBaseURL) throws UserManagementException, MailException, UserGroupManagementException;

    /**
     * @param validationBaseURL if <code>null</code>, no validation will be attempted
     */
    UserImpl createSimpleUser(String username, String email, String password, String fullName, String company, Locale locale, String validationBaseURL) throws UserManagementException, MailException, UserGroupManagementException;

    void updateSimpleUserPassword(String name, String newPassword) throws UserManagementException;

    void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL) throws UserManagementException;
    
    void updateUserProperties(String username, String fullName, String company, Locale locale) throws UserManagementException;

    SecurityUser createSocialUser(String username, SocialUserAccount socialUserAccount) throws UserManagementException, UserGroupManagementException;

    void deleteUser(String username) throws UserManagementException;

    /**
     * Creates a new role with initially empty {@link RoleDefinition#getPermissions() permissions}.
     */
    RoleDefinition createRoleDefinition(UUID id, String name);
    
    /**
     * Deletes the {@code roleDefinition} from this service persistently.
     */
    void deleteRoleDefinition(RoleDefinition roleDefinition);
    
    /**
     * The {@code roleDefinitionWithNewProperties} object represents an updated version, maybe a duplicate, of what we would get
     * when asking {@link #getRoleDefinition(UUID) this.getRole(roleWithNewProperties.getId())}. It may have changed compared to
     * what this service has in store. This service's representation (if not the same) and in particular the persistent
     * representation that this service will load upon its next start-up will be updated to match
     * {@code roleDefinitionWithNewProperties}'s state.
     */
    void updateRoleDefinition(RoleDefinition roleDefinitionWithNewProperties);
    
    Iterable<RoleDefinition> getRoleDefinitions();

    RoleDefinition getRoleDefinition(UUID idOfRoleDefinition);
    
    void addRoleForUser(SecurityUser user, Role role);

    void addRoleForUser(String username, Role role);

    void removeRoleFromUser(SecurityUser user, Role role);
    
    void removeRoleFromUser(String username, Role role);

    Iterable<WildcardPermission> getPermissionsFromUser(String username) throws UserManagementException;
    
    void removePermissionFromUser(String username, WildcardPermission permissionToRemove);

    void addPermissionForUser(String username, WildcardPermission permissionToAdd);

    /**
     * Registers a settings key together with its type. Calling this method is necessary for {@link #setSetting(String, Object)}
     * to have an effect for <code>key</code>. Calls to {@link #setSetting(String, Object)} will only accept values whose type
     * is compatible with <code>type</code>. Note that the store implementation may impose constraints on the types supported.
     * All store implementations are required to support at least {@link String} and {@link UUID} as types.
     */
    void addSetting(String key, Class<?> clazz) throws UserManagementException;

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

    void refreshSecurityConfig(ServletContext context);

    CacheManager getCacheManager();
    
    void sendMail(String username, String subject, String body) throws MailException;

    /**
     * Checks whether <code>password</code> is the correct password for the user identified by <code>username</code>
     * 
     * @throws UserManagementException
     *             in a user by that name does not exist
     */
    boolean checkPassword(String username, String password) throws UserManagementException;

    boolean checkPasswordResetSecret(String username, String passwordResetSecret) throws UserManagementException;

    /**
     * Generates a new random password for the user identified by <code>username</code> and sends it
     * to the user's e-mail address.
     */
    void resetPassword(String username, String baseURL) throws UserManagementException, MailException;

    boolean validateEmail(String username, String validationSecret) throws UserManagementException;

    /**
     * Permitted only for users with role {@link DefaultRoles#ADMIN} or when the subject's user name matches
     * <code>username</code>.
     * 
     * @param key must not be <code>null</code>
     * @param value must not be <code>null</code>
     */
    void setPreference(String username, String key, String value);

    void setPreferenceObject(String name, String preferenceKey, Object preference);

    /**
     * Permitted only for users with role {@link DefaultRoles#ADMIN} or when the subject's user name matches
     * <code>username</code>.
     */
    void unsetPreference(String username, String key);

    /**
     * @return <code>null</code> if no preference for the user identified by <code>username</code> is found
     */
    String getPreference(String username, String key);
    
    /**
     * @return all preferences of the given user
     */
    Map<String, String> getAllPreferences(String username);

    /**
     * Issues a new access token and remembers it so that later the user identified by <code>username</code> can be
     * authenticated using the token. Any access token previously created for same user will be invalidated by this
     * call.
     * 
     * @return a new access token if <code>username</code> identifies a known user, <code>null</code> otherwise
     */
    String createAccessToken(String username);

    /**
     * May be invoked by users with role {@link DefaultRoles#ADMIN} or the user identified by {@code username}. Returns
     * the last access token previously created by {@link #createAccessToken(String)} or {@code null} if no such access
     * token was created before for user {@code username} or was {@link #removeAccessToken(String)}.
     */
    String getAccessToken(String username);
    
    /**
     * Like {@link #getAccessToken(String)} only that instead of returning {@code null}, a new access token will
     * be created and returned instead (see {@link #createAccessToken(String)}.
     */
    String getOrCreateAccessToken(String username);

    /**
     * Looks up a user by an access token that was created before using {@link #createAccessToken(String)} for same user name.
     * 
     * @return <code>null</code> in case the access token is unknown or was deleted / invalidated
     */
    SecurityUser getUserByAccessToken(String accessToken);

    void removeAccessToken(String username);

    SecurityUser loginByAccessToken(String accessToken);
}
