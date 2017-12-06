package com.sap.sse.security.impl;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.TenantManagementException;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;

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
    Void internalCreateAcl(String idAsString, String displayName);
    
    Void internalAclPutPermissions(String idAsString, UUID group, Set<String> permissions);
    
    Void internalAclAddPermission(String idAsString, UUID group, String permission);
    
    Void internalAclRemovePermission(String idAsString, UUID group, String permission);
    
    Void internalDeleteAcl(String idAsString);
    
    Void internalCreateOwnership(String idOfOwnedObjectAsString, String owningUsername, UUID tenantOwnerId, String displayNameOfOwnedObject);
    
    Void internalDeleteOwnership(String idOfOwnedObjectAsString);
    
    Void internalCreateUserGroup(UUID id, String name) throws UserGroupManagementException;
    
    Void internalCreateTenant(UUID id, String name) throws TenantManagementException, UserGroupManagementException;
    
    Void internalUpdateUserGroup(UserGroup group);
    
    Void internalDeleteUserGroup(UUID id) throws UserGroupManagementException;
    
    Void internalDeleteTenant(UUID id) throws TenantManagementException, UserGroupManagementException;
    
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

    Void internalAddRoleForUser(String username, UUID role) throws UserManagementException;

    Void internalRemoveRoleFromUser(String username, UUID role) throws UserManagementException;

    Void internalAddPermissionForUser(String username, String permissionToAdd) throws UserManagementException;

    Void internalRemovePermissionForUser(String username, String permissionToRemove) throws UserManagementException;

    Void internalDeleteUser(String username) throws UserManagementException;

}
