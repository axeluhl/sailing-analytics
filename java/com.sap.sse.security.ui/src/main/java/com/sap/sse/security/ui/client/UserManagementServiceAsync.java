package com.sap.sse.security.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public interface UserManagementServiceAsync {
    void setOwnership(Ownership ownership, QualifiedObjectIdentifier idOfOwnedObject, String displayNameOfOwnedObject, AsyncCallback<QualifiedObjectIdentifier> callback);
    
    void getOwnership(QualifiedObjectIdentifier idOfOwnedObject, AsyncCallback<OwnershipAnnotation> callback);

    void getAccessControlLists(AsyncCallback<Collection<AccessControlListAnnotation>> callback);

    void getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, AsyncCallback<AccessControlListAnnotation> callback);

    void updateACL(QualifiedObjectIdentifier idOfAccessControlledObject, Map<String, Set<String>> permissionStrings, AsyncCallback<AccessControlList> callback);

    void addToACL(QualifiedObjectIdentifier idOfAccessControlledObject, String permission, String name, AsyncCallback<AccessControlList> callback);

    void removeFromACL(QualifiedObjectIdentifier idOfAccessControlledObject, String permission, String name, AsyncCallback<AccessControlList> callback);

    void getUserGroups(AsyncCallback<Collection<UserGroup>> callback);
    
    void getUserGroupByName(String userGroupName, AsyncCallback<UserGroup> callback);

    void createUserGroup(String name, String nameOfTenantOwner, AsyncCallback<UserGroup> callback);
    
    void deleteUserGroup(String userGroupIdAsString, AsyncCallback<SuccessInfo> asyncCallback);

    void addUserToUserGroup(String userGroupIdAsString, String username, AsyncCallback<Void> asyncCallback);

    void removeUserFromUserGroup(String tenantIdAsString, String username, AsyncCallback<Void> asyncCallback);

    void getUserList(AsyncCallback<Collection<UserDTO>> callback);
    
    void getUserByName(String username, AsyncCallback<UserDTO> callback);

    void getCurrentUser(AsyncCallback<UserDTO> callback);

    void login(String username, String password, AsyncCallback<SuccessInfo> callback);

    void logout(AsyncCallback<SuccessInfo> callback);

    void createSimpleUser(String name, String email, String password, String fullName, String company, String localeName, String validationBaseURL, String tenantOwner, AsyncCallback<UserDTO> callback);

    void updateSimpleUserPassword(String name, String oldPassword, String passwordResetSecret, String newPassword, AsyncCallback<Void> callback);

    void resetPassword(String username, String eMailAddress, String baseURL, AsyncCallback<Void> callback);
    
    void validateEmail(String username, String validationSecret, AsyncCallback<Boolean> markedAsyncCallback);

    void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL, AsyncCallback<Void> callback);

    void updateUserProperties(String username, String fullName, String company, String localeName,
            AsyncCallback<UserDTO> callback);

    void getFilteredSortedUserList(String filter, AsyncCallback<Collection<UserDTO>> callback);

    void createRoleDefinition(String roleDefinitionIdAsString, String name, AsyncCallback<RoleDefinition> callback);

    void deleteRoleDefinition(String roleDefinitionIdAsString, AsyncCallback<Void> callback);

    void updateRoleDefinition(RoleDefinition roleWithNewProperties, AsyncCallback<Void> callback);

    void getRoleDefinitions(AsyncCallback<ArrayList<RoleDefinition>> callback);

    void setRolesForUser(String username, Iterable<Triple<UUID, String, String>> roleDefinitionIdAndTenantNameAndUsernames, AsyncCallback<SuccessInfo> callback);

    void setPermissionsForUser(String username, Iterable<WildcardPermission> permissions, AsyncCallback<SuccessInfo> callback);

    void deleteUser(String username, AsyncCallback<SuccessInfo> callback);

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

    void verifySocialUser(CredentialDTO credential, AsyncCallback<UserDTO> callback);

}
