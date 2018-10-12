package com.sap.sse.security.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UnauthorizedException;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public interface UserManagementService extends RemoteService {
    QualifiedObjectIdentifier setOwnership(Ownership ownership, QualifiedObjectIdentifier idOfOwnedObject,
            String displayNameOfOwnedObject);
    
    Collection<AccessControlListAnnotation> getAccessControlLists() throws UnauthorizedException;

    AccessControlListAnnotation getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject);

    AccessControlList updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, Map<String, Set<String>> permissionStrings) throws UnauthorizedException;

    AccessControlList addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String groupOrTenantIdAsString, String action) throws UnauthorizedException;

    AccessControlList removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String groupOrTenantIdAsString, String action) throws UnauthorizedException;

    Collection<UserGroup> getUserGroups();

    UserGroup getUserGroupByName(String userGroupName) throws UnauthorizedException;

    UserGroup createUserGroup(String name, String tenantOwner) throws UserGroupManagementException, UnauthorizedException;

    SuccessInfo deleteUserGroup(String userGroupIdAsString) throws UserGroupManagementException, UnauthorizedException;
    
    void addUserToUserGroup(String userGroupIdAsString, String username) throws UnauthorizedException;

    void removeUserFromUserGroup(String userGroupIdAsString, String user) throws UnauthorizedException;

    Collection<UserDTO> getUserList() throws UnauthorizedException;

    UserDTO getUserByName(String username) throws UnauthorizedException;

    RoleDefinition createRoleDefinition(String roleDefinitionIdAsString, String name);
    
    void deleteRoleDefinition(String roleIdAsString);
    
    void updateRoleDefinition(RoleDefinition roleWithNewProperties);
    
    ArrayList<RoleDefinition> getRoleDefinitions();

    Collection<UserDTO> getFilteredSortedUserList(String filter) throws UnauthorizedException;

    Pair<UserDTO, UserDTO> getCurrentUser() throws UnauthorizedException;

    SuccessInfo login(String username, String password);

    UserDTO createSimpleUser(String name, String email, String password, String fullName, String company, String localeName, String validationBaseURL, String tenantOwner) throws UserManagementException, MailException, UnauthorizedException;
    
    /**
     * Either <code>oldPassword</code> or <code>passwordResetSecret</code> need to be provided, or the current user needs to have
     * the {@link DefaultRoles#ADMIN} role to be able to set the new password.
     */
    void updateSimpleUserPassword(String name, String oldPassword, String passwordResetSecret, String newPassword) throws UserManagementException;

    void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL) throws UserManagementException, MailException;

    UserDTO updateUserProperties(String username, String fullName, String company, String localeName) throws UserManagementException;

    void resetPassword(String username, String eMailAddress, String baseURL) throws UserManagementException, MailException;

    boolean validateEmail(String username, String validationSecret) throws UserManagementException;

    SuccessInfo deleteUser(String username) throws UnauthorizedException;

    SuccessInfo logout();

    SuccessInfo setRolesForUser(String username,
            Iterable<Triple<UUID, String, String>> roleDefinitionIdAndTenantQualifierNameAndUsernames)
            throws UnauthorizedException;

    SuccessInfo setPermissionsForUser(String username, Iterable<WildcardPermission> permissions) throws UnauthorizedException;

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

    public Pair<UserDTO, UserDTO> verifySocialUser(CredentialDTO credential) throws OAuthException;

    OwnershipAnnotation getOwnership(QualifiedObjectIdentifier idOfOwnedObject);
}
