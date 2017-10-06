package com.sap.sse.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;

public class AccessControlListWithStore implements AccessControlList {    
    private final String idAsString;
    private final String displayName;
    
    private final UserStore userStore;
    
    /**
     * Maps from UserGroup name to its permissions
     */
    private final Map<UUID, Set<String>> permissionMap;
    
    public AccessControlListWithStore(String idAsString, String displayName, Map<UUID, Set<String>> permissionMap, UserStore userStore) {
        this.idAsString = idAsString;
        this.displayName = displayName;
        this.permissionMap = permissionMap;
        this.userStore = userStore;
    }
    
    public AccessControlListWithStore(String idAsString, String displayName, UserStore userStore) {
        this(idAsString, displayName, new HashMap<>(), userStore);
    }
    
    @Override
    public PermissionChecker.PermissionState hasPermission(String username, String action) {
        for (Map.Entry<UUID, Set<String>> entry : permissionMap.entrySet()) {
            UserGroup group = userStore.getUserGroup(entry.getKey());
            if (group.contains(username)) {
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
