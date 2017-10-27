package com.sap.sse.security;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;

public interface AccessControlStore extends Named {
    Iterable<AccessControlList> getAccessControlLists();
    AccessControlList getAccessControlList(String idAsString);
    /**
     * @param idAsString ID of the object the ACL is attached to
     * @param owner Owner of the object the ACL is attached to
     */
    AccessControlList createAccessControlList(String idAsString, String displayName);
    AccessControlStore putAclPermissions(String idAsString, UUID group, Set<String> permissions);
    AccessControlStore addAclPermission(String idAsString, UUID group, String permission);
    AccessControlStore removeAclPermission(String idAsString, UUID group, String permission);
    AccessControlStore removeAccessControlList(String idAsString);
    
    Iterable<Owner> getOwnerships();
    Owner getOwnership(String idAsString);
    Owner createOwnership(String idAsString, String owner, UUID tenantOwner, String displayName);
    AccessControlStore setOwnership(String idAsString, String owner, UUID tenantOwner, String displayName);
    AccessControlStore removeOwnership(String idAsString);
    
    Iterable<Role> getRoles();
    Role getRole(UUID id);
    Role createRole(UUID id, String displayName, Set<String> permissions);
    AccessControlStore setRolePermissions(UUID id, Set<String> permissions);
    AccessControlStore addRolePermission(UUID id, String permission);
    AccessControlStore removeRolePermission(UUID id, String permission);
    AccessControlStore setRoleDisplayName(UUID id, String displayName);
    AccessControlStore removeRole(UUID id);
    
    void clear();
    void replaceContentsFrom(AccessControlStore newAclStore);
}