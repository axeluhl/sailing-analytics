package com.sap.sse.security.ui.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.shared.TenantManagementException;
import com.sap.sse.security.shared.UnauthorizedException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.AccessControlListDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.TenantDTO;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserGroupDTO;

public interface UserManagementService extends RemoteService {
    Collection<AccessControlListDTO> getAccessControlListList() throws UnauthorizedException;

    AccessControlListDTO getAccessControlList(String idAsString);

    AccessControlListDTO updateACL(String idAsString, Map<String, Set<String>> permissionStrings) throws UnauthorizedException;

    AccessControlListDTO addToACL(String idAsString, String tenantIdAsString, String permission) throws UnauthorizedException;

    AccessControlListDTO removeFromACL(String idAsString, String tenantIdAsString, String permission) throws UnauthorizedException;

    Collection<TenantDTO> getTenantList() throws UnauthorizedException;

    TenantDTO createTenant(String name, String tenantOwner) throws TenantManagementException, UnauthorizedException;

    UserGroupDTO addUserToTenant(String idAsString, String user) throws UnauthorizedException;

    UserGroupDTO removeUserFromTenant(String idAsString, String user) throws UnauthorizedException;

    SuccessInfo deleteTenant(String idAsString) throws UnauthorizedException;

    Collection<UserDTO> getUserList() throws UnauthorizedException;

    Collection<UserDTO> getFilteredSortedUserList(String filter) throws UnauthorizedException;

    UserDTO getCurrentUser() throws UnauthorizedException;

    SuccessInfo login(String username, String password);

    UserDTO createSimpleUser(String name, String email, String password, String fullName, String company, String localeName, String validationBaseURL, String tenantOwner) throws UserManagementException, MailException, UnauthorizedException;
    
    /**
     * Either <code>oldPassword</code> or <code>passwordResetSecret</code> need to be provided, or the current user needs to have
     * the {@link DefaultRoles#ADMIN} role to be able to set the new password.
     */
    void updateSimpleUserPassword(String name, String oldPassword, String passwordResetSecret, String newPassword) throws UserManagementException;

    void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL) throws UserManagementException, MailException;

    void updateUserProperties(String username, String fullName, String company, String localeName) throws UserManagementException;

    void resetPassword(String username, String eMailAddress, String baseURL) throws UserManagementException, MailException;

    boolean validateEmail(String username, String validationSecret) throws UserManagementException;

    SuccessInfo deleteUser(String username) throws UnauthorizedException;

    SuccessInfo logout();

    SuccessInfo setRolesForUser(String username, Iterable<UUID> roles) throws UnauthorizedException;

    SuccessInfo setPermissionsForUser(String username, Iterable<String> permissions) throws UnauthorizedException;

    Map<String, String> getSettings();

    Map<String, String> getSettingTypes();

    void setSetting(String key, String clazz, String setting);

    void addSetting(String key, String clazz, String setting);

    /**
     * Permitted only for users with role {@link DefaultRoles#ADMIN} or when the subject's user name matches
     * <code>username</code>.
     * 
     * @param key must not be <code>null</code>
     * @param value must not be <code>null</code>
     * @throws UserManagementException 
     */
    void setPreference(String username, String key, String value) throws UserManagementException, UnauthorizedException;
    
    void setPreferences(String username, Map<String, String> keyValuePairs) throws UserManagementException, UnauthorizedException;

    /**
     * Permitted only for users with role {@link DefaultRoles#ADMIN} or when the subject's user name matches
     * <code>username</code>.
     */
    void unsetPreference(String username, String key) throws UserManagementException, UnauthorizedException;

    /**
     * @return <code>null</code> if no preference for the user identified by <code>username</code> is found
     */
    String getPreference(String username, String key) throws UserManagementException, UnauthorizedException;
    
    Map<String, String> getPreferences(String username, List<String> keys) throws UserManagementException, UnauthorizedException;
    
    Map<String, String> getAllPreferences(String username) throws UserManagementException, UnauthorizedException;

    String getAccessToken(String username);

    String getOrCreateAccessToken(String username);

    // ------------------------------------------------ OAuth Interface --------------------------------------------------------------

    public String getAuthorizationUrl(CredentialDTO credential) throws OAuthException;

    public UserDTO verifySocialUser(CredentialDTO credential) throws OAuthException;

}
