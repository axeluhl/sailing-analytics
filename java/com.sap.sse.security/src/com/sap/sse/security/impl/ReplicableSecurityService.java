package com.sap.sse.security.impl;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.shared.TenantManagementException;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Publishes those methods of {@link SecurityServiceImpl} that are required by operations implemented as lambda
 * expressions to fulfill their tasks. These operations should not be invoked by external service clients.
 * {@link SecurityService} is the one registered with the OSGi registry and thus the publicly-visible
 * interface.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReplicableSecurityService extends SecurityService {
    Void internalCreateAcl(String idOfAccessControlledObjectAsString, String displayName);
    
    Void internalAclPutPermissions(String idOfAccessControlledObjectAsString, UUID groupId, Set<String> actions);
    
    Void internalAclAddPermission(String idOfAccessControlledObjectAsString, UUID groupId, String action);
    
    Void internalAclRemovePermission(String idOfAccessControlledObjectAsString, UUID groupId, String action);
    
    Void internalDeleteAcl(String idOfAccessControlledObjectAsString);
    
    Void internalCreateOwnership(String idOfOwnedObjectAsString, String owningUsername, UUID tenantOwnerId, String displayNameOfOwnedObject);
    
    Void internalDeleteOwnership(String idOfOwnedObjectAsString);
    
    Void internalCreateUserGroup(UUID groupId, String name) throws UserGroupManagementException;
    
    Void internalDeleteUserGroup(UUID groupId) throws UserGroupManagementException;
    
    Void internalAddUserToUserGroup(UUID groupId, String username) throws UserGroupManagementException;

    Void internalRemoveUserFromUserGroup(UUID groupId, String username) throws UserGroupManagementException;
    
    Void internalCreateTenant(UUID groupId, String name) throws TenantManagementException, UserGroupManagementException;
    
    Void internalAddUserToTenant(UUID tenantId, String username) throws UserGroupManagementException;

    Void internalRemoveUserFromTenant(UUID tenantId, String username) throws UserGroupManagementException;
    
    Void internalDeleteTenant(UUID tenantId) throws TenantManagementException, UserGroupManagementException;
    
    Void internalStoreUser(UserImpl user);

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

    Void internalAddRoleForUser(String username, UUID role) throws UserManagementException;

    Void internalRemoveRoleFromUser(String username, UUID role) throws UserManagementException;

    Void internalAddPermissionForUser(String username, WildcardPermission permissionToAdd) throws UserManagementException;

    Void internalRemovePermissionForUser(String username, WildcardPermission permissionToRemove) throws UserManagementException;

    Void internalDeleteUser(String username) throws UserManagementException;

}
