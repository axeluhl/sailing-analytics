package com.sap.sse.security.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListAnnotationDTO;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipAnnotationDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.dto.WildcardPermissionWithSecurityDTO;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;

public interface UserManagementServiceAsync {
    void setOwnership(OwnershipDTO ownership, QualifiedObjectIdentifier idOfOwnedObject,
            String displayNameOfOwnedObject, AsyncCallback<QualifiedObjectIdentifier> callback);
    
    void getOwnership(QualifiedObjectIdentifier idOfOwnedObject, AsyncCallback<OwnershipAnnotationDTO> callback);

    void getAccessControlLists(AsyncCallback<Collection<AccessControlListAnnotationDTO>> callback);

    void getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, AsyncCallback<AccessControlListAnnotationDTO> callback);

    void updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            Map<String, Set<String>> permissionStrings, AsyncCallback<AccessControlListDTO> callback);

    void getAccessControlListWithoutPruning(QualifiedObjectIdentifier idOfAccessControlledObject,
            AsyncCallback<AccessControlListDTO> updateAclAsyncCallback);

    void overrideAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, AccessControlListDTO acl,
            AsyncCallback<AccessControlListDTO> updateAclAsyncCallback);

    void addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String permission, String name,
            AsyncCallback<AccessControlListDTO> callback);

    void removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String permission,
            String name, AsyncCallback<AccessControlListDTO> callback);

    /**
     * Returns those user groups the requesting user can read
     */
    void getUserGroups(AsyncCallback<Collection<UserGroupDTO>> callback);
    
    void getUserGroupByName(String userGroupName, AsyncCallback<UserGroupDTO> callback);
    
    void getStrippedUserGroupByName(String userGroupName, AsyncCallback<StrippedUserGroupDTO> callback);

    void createUserGroup(String name, AsyncCallback<UserGroupDTO> callback);
    
    void deleteUserGroup(String userGroupIdAsString, AsyncCallback<SuccessInfo> asyncCallback);

    void addUserToUserGroup(String userGroupIdAsString, String username, AsyncCallback<Void> asyncCallback);

    void removeUserFromUserGroup(String tenantIdAsString, String username, AsyncCallback<Void> asyncCallback);

    void putRoleDefintionToUserGroup(String userGroupIdAsString, String roleDefinitionIdAsString, boolean forAll,
            AsyncCallback<Void> callback);

    void removeRoleDefintionFromUserGroup(String userGroupIdAsString, String roleDefinitionIdAsString,
            AsyncCallback<Void> callback);

    /**
     * Returns those users the requesting user can read
     */
    void getUserList(AsyncCallback<Collection<UserDTO>> callback);
    
    void getUserByName(String username, AsyncCallback<UserDTO> callback);

    void getCurrentUser(AsyncCallback<Triple<UserDTO, UserDTO, ServerInfoDTO>> callback);

    void login(String username, String password, AsyncCallback<SuccessInfo> callback);

    void logout(AsyncCallback<SuccessInfo> callback);

    void createSimpleUser(String name, String email, String password, String fullName, String company,
            String localeName, String validationBaseURL, AsyncCallback<UserDTO> callback);

    void updateSimpleUserPassword(String name, String oldPassword, String passwordResetSecret, String newPassword, AsyncCallback<Void> callback);

    void resetPassword(String username, String eMailAddress, String baseURL, AsyncCallback<Void> callback);
    
    void validateEmail(String username, String validationSecret, AsyncCallback<Boolean> markedAsyncCallback);

    void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL, AsyncCallback<Void> callback);

    void updateUserProperties(String username, String fullName, String company, String localeName,
            String defaultTenantIdAsString, AsyncCallback<UserDTO> callback);

    void createRoleDefinition(String roleDefinitionIdAsString, String name, AsyncCallback<RoleDefinitionDTO> callback);

    void deleteRoleDefinition(String roleDefinitionIdAsString, AsyncCallback<Void> callback);

    void updateRoleDefinition(RoleDefinitionDTO roleWithNewProperties, AsyncCallback<Void> callback);

    void getRoleDefinitions(AsyncCallback<ArrayList<RoleDefinitionDTO>> callback);

    void setRolesForUser(String username, Iterable<Triple<UUID, String, String>> roleDefinitionIdAndTenantNameAndUsernames, AsyncCallback<SuccessInfo> callback);

    void setPermissionsForUser(String username, Iterable<WildcardPermission> permissions, AsyncCallback<SuccessInfo> callback);

    void deleteUser(String username, AsyncCallback<SuccessInfo> callback);
    
    void deleteUsers(Set<String> usernames, AsyncCallback<Set<SuccessInfo>> callback);

    void getSettings(AsyncCallback<Map<String, String>> callback);

    void setSetting(String key, String clazz, String setting, AsyncCallback<Void> callback);

    void getSettingTypes(AsyncCallback<Map<String, String>> callback);
    
    void addSetting(String key, String clazz, String setting, AsyncCallback<Void> callback);

    void setPreference(String username, String key, String value, AsyncCallback<Void> callback);
    
    void setPreferences(String username, Map<String, String> keyValuePairs, AsyncCallback<Void> callback);

    void unsetPreference(String username, String key, AsyncCallback<Void> callback);

    void getPreference(String username, String key, AsyncCallback<String> callback);
    
    void getPreferences(String username, List<String> keys,
            final AsyncCallback<Map<String, String>> callback);
    
    void getAllPreferences(String username, final AsyncCallback<Map<String, String>> callback);

    /**
     * Obtains an access token for the user specified by {@code username}. The caller needs to
     * have role {@link DefaultRoles#ADMIN} or be authorized as the user identified by {@code username}
     * in order to be permitted to retrieve the access token. 
     */
    void getAccessToken(String username, AsyncCallback<String> markedAsyncCallback);

    /**
     * Like {@link #getAccessToken(String, AsyncCallback)}, only that instead of returning {@code null} a
     * new access token will be created and returned.
     */
    void getOrCreateAccessToken(String username, AsyncCallback<String> callback);

  //------------------------------------------------ OAuth Interface ----------------------------------------------------------------------

    void getAuthorizationUrl(CredentialDTO credential, AsyncCallback<String> callback);

    void verifySocialUser(CredentialDTO credential, AsyncCallback<Triple<UserDTO, UserDTO, ServerInfoDTO>> markedAsyncCallback);

    /**
     * Grants the role associated with the given {@code roleDefinitionId}, {@code userQualifierName} and
     * {@code tenantQualifierName} to the given {@code username} if the current user has the required permissions
     */
    void addRoleToUser(String username, String userQualifierName, UUID roleDefinitionId, String tenantQualifierName,
            AsyncCallback<SuccessInfo> callback);

    /**
     * Revokes the role associated with the given {@code roleDefinitionId}, {@code userQualifierName} and
     * {@code tenantQualifierName} for the given {@code username} if the current user has the required permissions
     */
    void removeRoleFromUser(String username, String userQualifierName, UUID roleDefinitionId,
            String tenantQualifierName,
            AsyncCallback<SuccessInfo> callback);

    /**
     * Grants the given {@code permission} to the given {@code username} if the current user has the required
     * permissions
     */
    void addPermissionForUser(String username, WildcardPermission permissions,
            AsyncCallback<SuccessInfo> callback);

    /**
     * Revokes the given {@code permission} for the given {@code username} if the current user has the required
     * permissions
     */
    void removePermissionFromUser(String username, WildcardPermissionWithSecurityDTO permission,
            AsyncCallback<SuccessInfo> callback);
}
