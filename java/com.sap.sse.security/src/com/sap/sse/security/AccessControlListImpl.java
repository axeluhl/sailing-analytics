package com.sap.sse.security;

import java.io.Serializable;
import java.util.Collections;
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
    private final Map<UUID, Set<String>> permissionsByUserGroupId;
    
    public AccessControlListImpl(String idAsString, String displayName, Map<UUID, Set<String>> permissionsByUserGroupId) {
        this.idAsString = idAsString;
        this.displayName = displayName;
        this.permissionsByUserGroupId = permissionsByUserGroupId;
    }
    
    public AccessControlListImpl(String idAsString, String displayName) {
        this(idAsString, displayName, new HashMap<>());
    }
    
    @Override
    public PermissionChecker.PermissionState hasPermission(String username, String action, Iterable<UserGroup> userGroups) {
        for (Map.Entry<UUID, Set<String>> entry : permissionsByUserGroupId.entrySet()) {
            for (UserGroup userGroup : userGroups) {
                if (userGroup.getId().equals(entry.getKey()) && userGroup.contains(username)) {
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
        return Collections.unmodifiableMap(permissionsByUserGroupId);
    }
}
