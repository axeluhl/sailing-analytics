package com.sap.sse.security.shared;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.NamedWithID;

public interface AccessControlList extends NamedWithID {    
    PermissionChecker.PermissionState hasPermission(String user, String action);
    
    Map<String, Set<String>> getPermissionMap();
}