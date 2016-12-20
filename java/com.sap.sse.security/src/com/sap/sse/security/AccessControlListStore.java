package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.common.Named;

public interface AccessControlListStore extends Named {
    AccessControlList getAccessControlListByName(String name);
    
    /**
     * @param id ID of the object the ACL is attached to
     * @param owner Owner of the object the ACL is attached to
     */
    AccessControlList createAccessControlList(String id, String owner);
    AccessControlListStore putPermissions(String id, String group, Set<String> permissions);
    AccessControlListStore addPermission(String id, String group, String permission);
    AccessControlListStore removePermission(String id, String group, String permission);
    AccessControlListStore removeAccessControlList(String id);
}
