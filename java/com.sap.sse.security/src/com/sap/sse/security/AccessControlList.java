package com.sap.sse.security;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.security.shared.Permission;

public interface AccessControlList extends NamedWithID {
    Tenant getOwner();
    
    boolean hasPermission(User user, Permission permission);
    
    AccessControlList putPermissions(UserGroup group, Set<Permission> permissions);
    AccessControlList addPermission(UserGroup group, Permission permission);
    AccessControlList removePermission(UserGroup group, Permission permission);
    
    Map<UserGroup, Set<Permission>> getPermissionMap();
}