package com.sap.sse.security;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.osgi.framework.BundleContext;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;
import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.operations.SecurityOperation;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UserManagementException;

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

    Iterable<User> getUserList();

    User getUserByName(String username);

    User getUserByEmail(String email);

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
    User createSimpleUser(String username, String email, String password, String fullName, String company, String validationBaseURL) throws UserManagementException, MailException;
    
    /**
     * @param validationBaseURL if <code>null</code>, no validation will be attempted
     */
    User createSimpleUser(String username, String email, String password, String fullName, String company, Locale locale, String validationBaseURL) throws UserManagementException, MailException;

    void updateSimpleUserPassword(String name, String newPassword) throws UserManagementException;

    void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL) throws UserManagementException;
    
    void updateUserProperties(String username, String fullName, String company, Locale locale) throws UserManagementException;

    User createSocialUser(String username, SocialUserAccount socialUserAccount) throws UserManagementException;

    void deleteUser(String username) throws UserManagementException;

    Iterable<String> getRolesFromUser(String username) throws UserManagementException;

    void addRoleForUser(String username, String role);

    void removeRoleFromUser(String username, String role);

    Iterable<String> getPermissionsFromUser(String username) throws UserManagementException;
    
    void removePermissionFromUser(String username, String permissionToRemove);

    void addPermissionForUser(String username, String permissionToAdd);

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
    User getUserByAccessToken(String accessToken);

    void removeAccessToken(String username);

    User loginByAccessToken(String accessToken);

}
