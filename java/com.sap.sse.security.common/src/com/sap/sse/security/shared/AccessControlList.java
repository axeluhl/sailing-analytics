package com.sap.sse.security.shared;

import java.util.Map;
import java.util.Set;

import com.sap.sse.common.NamedWithID;

public interface AccessControlList extends NamedWithID {    
    boolean hasPermission(String user, String permission);
    
    Map<String, Set<String>> getPermissionMap();
}