package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;

public interface AccessControlStore extends Named {
    Iterable<AccessControlList> getAccessControlLists();
    AccessControlList getAccessControlListByName(String name);
    Owner getOwnership(String id);
    
    /**
     * @param id ID of the object the ACL is attached to
     * @param owner Owner of the object the ACL is attached to
     */
    AccessControlList createAccessControlList(String id);
    AccessControlStore putPermissions(String id, String group, Set<String> permissions);
    AccessControlStore addPermission(String id, String group, String permission);
    AccessControlStore removePermission(String id, String group, String permission);
    AccessControlStore removeAccessControlList(String id);
    
    Iterable<Owner> getOwnerships();
    Owner createOwnership(String id, String owner, String tenantOwner);
    AccessControlStore setOwnership(String id, String owner, String tenantOwner);
    AccessControlStore removeOwnership(String id);
    
    void clear();
    void replaceContentsFrom(AccessControlStore newAclStore);
   
}
