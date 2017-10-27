package com.sap.sse.security.ui.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;
import com.sap.sse.security.shared.UserGroup;

public class AccessControlListDTO implements AccessControlList, IsSerializable {
    private String idAsString;
    private String displayName;
    
    private Map<UserGroupDTO, Set<String>> permissionMap;
    
    AccessControlListDTO() {} // for serialization only
    
    public AccessControlListDTO(String idAsString, String displayName, Map<UserGroupDTO, Set<String>> permissionMap) {
        this.idAsString = idAsString;
        this.displayName = displayName;
        this.permissionMap = permissionMap;
    }
    
    @Override
    public String getId() {
        return idAsString;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public PermissionChecker.PermissionState hasPermission(String user, String action, Iterable<UserGroup> tenants) {
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
    public Map<UUID, Set<String>> getPermissionMap() {
        Map<UUID, Set<String>> permissionMap = new HashMap<>();
        for (Map.Entry<UserGroupDTO, Set<String>> entry : this.permissionMap.entrySet()) {
            permissionMap.put(entry.getKey().getId(), entry.getValue());
        }
        return permissionMap;
    }
    
    public Map<UserGroupDTO, Set<String>> getUserGroupPermissionMap() {
        return permissionMap;
    }
}
