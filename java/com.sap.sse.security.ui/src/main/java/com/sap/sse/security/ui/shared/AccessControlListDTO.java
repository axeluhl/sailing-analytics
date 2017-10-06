package com.sap.sse.security.ui.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;

public class AccessControlListDTO implements AccessControlList, IsSerializable {
    private String id;
    private String displayName;
    
    private Map<UserGroupDTO, Set<String>> permissionMap;
    
    AccessControlListDTO() {} // for serialization only
    
    public AccessControlListDTO(String id, String displayName, Map<UserGroupDTO, Set<String>> permissionMap) {
        this.id = id;
        this.permissionMap = permissionMap;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public PermissionChecker.PermissionState hasPermission(String user, String action) {
        for (Map.Entry<UserGroupDTO, Set<String>> entry : permissionMap.entrySet()) {
            if (entry.getKey().contains(user)) {
                if (entry.getValue().contains("!" + action)) {
                    return PermissionState.REVOKED;
                } else if (entry.getValue().contains(action)) {
                    return PermissionState.GRANTED;
                }
            }
        }
        return PermissionState.NONE;
    }
    
    @Override
    public Map<String, Set<String>> getPermissionMap() {
        Map<String, Set<String>> permissionMap = new HashMap<>();
        for (Map.Entry<UserGroupDTO, Set<String>> entry : this.permissionMap.entrySet()) {
            permissionMap.put(entry.getKey().getName(), entry.getValue());
        }
        return permissionMap;
    }
    
    public Map<UserGroupDTO, Set<String>> getUserGroupPermissionMap() {
        return permissionMap;
    }
}
