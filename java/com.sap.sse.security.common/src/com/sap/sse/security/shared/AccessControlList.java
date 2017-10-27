package com.sap.sse.security.shared;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.WithID;

public interface AccessControlList extends WithID {    
    PermissionChecker.PermissionState hasPermission(String user, String action, Iterable<UserGroup> tenants);
    
    String getDisplayName();
    
    Map<UUID, Set<String>> getPermissionMap();
}