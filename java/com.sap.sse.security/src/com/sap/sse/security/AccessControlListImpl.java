package com.sap.sse.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;
import com.sap.sse.security.shared.UserGroup;

public class AccessControlListImpl implements AccessControlList {    
    private final String idAsString;
    private final String displayName;
    
    /**
     * Maps from UserGroup name to its permissions
     */
    private final Map<UUID, Set<String>> permissionMap;
    
    public AccessControlListImpl(String idAsString, String displayName, Map<UUID, Set<String>> permissionMap) {
        this.idAsString = idAsString;
        this.displayName = displayName;
        this.permissionMap = permissionMap;
    }
    
    public AccessControlListImpl(String idAsString, String displayName) {
        this(idAsString, displayName, new HashMap<>());
    }
    
    @Override
    public PermissionChecker.PermissionState hasPermission(String username, String action, Iterable<UserGroup> tenants) {
        for (Map.Entry<UUID, Set<String>> entry : permissionMap.entrySet()) {
            for (UserGroup tenant : tenants) {
                if (tenant.getId().equals(entry.getKey()) && tenant.contains(username)) {
                    if (entry.getValue().contains("!" + action)) {
                        return PermissionState.REVOKED;
                    } else if (entry.getValue().contains(action)) {
                        return PermissionState.GRANTED;
                    }
                }
            }
        }
        return PermissionState.NONE;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Serializable getId() {
        return idAsString;
    }

    @Override
    public Map<UUID, Set<String>> getPermissionMap() {
        return permissionMap;
    }
}
