package com.sap.sse.security.impl;

import java.util.Set;
import java.util.UUID;

import org.apache.shiro.session.Session;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.User;

/**
 * Publishes those methods of {@link SecurityServiceImpl} that are required by operations implemented as lambda
 * expressions to fulfill their tasks. These operations should not be invoked by external service clients.
 * {@link SecurityService} is the one registered with the OSGi registry and thus the publicly-visible interface.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface ReplicableSecurityService extends SecurityService {
    Void internalSetEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String displayName);
    
    Void internalAclPutPermissions(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId, Set<String> actions);
    
    Void internalAclAddPermission(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId, String action);
    
    Void internalAclRemovePermission(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId, String action);
    
    Void internalDeleteAcl(QualifiedObjectIdentifier idOfAccessControlledObject);
    
    Ownership internalSetOwnership(QualifiedObjectIdentifier idOfOwnedObject, String owningUsername, UUID tenantOwnerId, String displayNameOfOwnedObject);
    
    Void internalDeleteOwnership(QualifiedObjectIdentifier idOfOwnedObject);
    
    Void internalCreateUserGroup(UUID groupId, String name) throws UserGroupManagementException;
    
    Void internalDeleteUserGroup(UUID groupId) throws UserGroupManagementException;
    
    Void internalAddUserToUserGroup(UUID groupId, String username) throws UserGroupManagementException;

    Void internalRemoveUserFromUserGroup(UUID groupId, String username) throws UserGroupManagementException;
    
    Void internalPutRoleDefinitionToUserGroup(UUID groupId, UUID roleDefinitionId, boolean forAll)
            throws UserGroupManagementException;

    Void internalRemoveRoleDefinitionFromUserGroup(UUID groupId, UUID roleDefinitionId)
            throws UserGroupManagementException;

    Void internalStoreUser(User user);

    Void internalSetPreference(String username, String key, String value);

    /**
     * @return the {@link String}-ified preference object value
     */
    String internalSetPreferenceObject(String username, String key, Object value);

    Void internalUnsetPreference(String username, String key);
    
    Void internalSetAccessToken(String username, String accessToken);

    Void internalRemoveAccessToken(String username);

    Boolean internalSetSetting(String key, Object setting);

    Void internalAddSetting(String key, Class<?> clazz);

    Void internalAddRoleForUser(String username, UUID roleDefinitionId, UUID idOfTenantQualifyingRole, String nameOfUserQualifyingRole) throws UserManagementException;

    Void internalRemoveRoleFromUser(String username, UUID roleDefinitionId, UUID idOfTenantQualifyingRole, String nameOfUserQualifyingRole) throws UserManagementException;

    Void internalAddPermissionForUser(String username, WildcardPermission permissionToAdd) throws UserManagementException;

    Void internalRemovePermissionForUser(String username, WildcardPermission permissionToRemove) throws UserManagementException;

    Void internalDeleteUser(String username) throws UserManagementException;

    RoleDefinition internalCreateRoleDefinition(UUID roleDefinitionId, String name);
    
    Void internalDeleteRoleDefinition(UUID roleDefinitionId);
    
    Void internalUpdateRoleDefinition(RoleDefinition roleDefinitionWithNewProperties);

    void storeSession(String cacheName, Session value);

    void removeSession(String cacheName, Session result);

    void removeAllSessions(String cacheName);

    Void internalSetDefaultTenantForServerForUser(String username, UUID defaultTenantId, String serverName);

}
