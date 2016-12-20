package com.sap.sse.security;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.NamedWithID;

public interface AccessControlList extends NamedWithID {
    String getOwner();
    
    boolean hasPermission(String user, String permission);
    
    AccessControlList putPermissions(String group, Set<String> permissions);
    AccessControlList addPermission(String group, String permission);
    AccessControlList removePermission(String group, String permission);
    
    Map<String, Set<String>> getPermissionMap();
}