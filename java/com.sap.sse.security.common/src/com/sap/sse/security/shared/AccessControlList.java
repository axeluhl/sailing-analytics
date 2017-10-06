package com.sap.sse.security.shared;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.WithID;

public interface AccessControlList extends WithID {    
    PermissionChecker.PermissionState hasPermission(String user, String action);
    
    String getDisplayName();
    
    Map<String, Set<String>> getPermissionMap();
}