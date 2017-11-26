package com.sap.sse.security;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.WildcardPermission;

public interface AccessControlStore extends Named {
    Iterable<AccessControlList> getAccessControlLists();
    AccessControlList getAccessControlList(String idAsString);
    /**
     * @param idAsString ID of the object the ACL is attached to
     * @param owner Owner of the object the ACL is attached to
     */
    AccessControlList createAccessControlList(String idAsString, String displayName);
    AccessControlStore setAclPermissions(String idAsString, UUID group, Set<String> permissions);
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
    Role createRole(UUID id, String displayName, Set<WildcardPermission> permissions);
    AccessControlStore setRolePermissions(UUID id, Set<WildcardPermission> permissions);
    AccessControlStore addRolePermission(UUID id, WildcardPermission permission);
    AccessControlStore removeRolePermission(UUID id, WildcardPermission permission);
    AccessControlStore setRoleDisplayName(UUID id, String displayName);
    AccessControlStore removeRole(UUID id);
    
    void clear();
    void replaceContentsFrom(AccessControlStore newAccessControlStore);
}