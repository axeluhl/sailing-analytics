package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.Permission;

public interface AccessControlListStore extends Named {
    AccessControlList getAccessControlListByName(String name);
    
    /**
     * @param id ID of the object the ACL is attached to
     * @param owner Owner of the object the ACL is attached to
     */
    AccessControlList createAccessControlList(String id, Tenant owner);
    AccessControlListStore putPermissions(String id, UserGroup group, Set<Permission> permissions);
    AccessControlListStore addPermission(String id, UserGroup group, Permission permission);
    AccessControlListStore removePermission(String id, UserGroup group, Permission permission);
    AccessControlListStore removeAccessControlList(String id);
}
