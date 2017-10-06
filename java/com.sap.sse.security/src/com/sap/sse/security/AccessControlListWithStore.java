package com.sap.sse.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;

public class AccessControlListWithStore implements AccessControlList {    
    private final String id;
    private final String displayName;
    
    private final UserStore userStore;
    
    /**
     * Maps from UserGroup name to its permissions
     */
    private final Map<String, Set<String>> permissionMap;
    
    public AccessControlListWithStore(String id, String displayName, Map<String, Set<String>> permissionMap, UserStore userStore) {
        this.id = id;
        this.displayName = displayName;
        this.permissionMap = permissionMap;
        this.userStore = userStore;
    }
    
    public AccessControlListWithStore(String id, String displayName, UserStore userStore) {
        this(id, displayName, new HashMap<>(), userStore);
    }
    
    @Override
    public PermissionChecker.PermissionState hasPermission(String username, String action) {
        for (Map.Entry<String, Set<String>> entry : permissionMap.entrySet()) {
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
        return id;
    }

    @Override
    public Map<String, Set<String>> getPermissionMap() {
        return permissionMap;
    }
}
