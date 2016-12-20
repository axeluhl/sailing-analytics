package com.sap.sse.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AccessControlListWithStore implements AccessControlList {
    private static final long serialVersionUID = -5709064967680495227L;
    
    private final String id;
    private String owner;
    
    private final AccessControlListStore store;
    private final UserStore userStore;
    
    /**
     * Maps from UserGroup name to its permissions
     */
    private final Map<String, Set<String>> permissionMap;
    
    public AccessControlListWithStore(String id, String owner, Map<String, Set<String>> permissionMap, UserStore userStore, AccessControlListStore store) {
        this.id = id;
        this.owner = owner;
        this.permissionMap = permissionMap;
        this.store = store;
        this.userStore = userStore;
    }
    
    public AccessControlListWithStore(String id, String owner, UserStore userStore, AccessControlListStore store) {
        this(id, owner, new HashMap<>(), userStore, store);
    }
    
    @Override
    public boolean hasPermission(String username, String permission) {
        for (Map.Entry<String, Set<String>> entry : permissionMap.entrySet()) {
            UserGroup group = userStore.getUserGroupByName(entry.getKey());
            if (group.contains(username) && entry.getValue().contains(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AccessControlList putPermissions(String group, Set<String> permissions) {
        permissionMap.put(group,  permissions);
        store.putPermissions(id, group, permissions);
        return this;
    }

    @Override
    public AccessControlList addPermission(String group, String permission) {
        Set<String> permissionsForGroup = permissionMap.get(group);
        if (permissionsForGroup != null) {
            permissionsForGroup.add(permission);
            store.addPermission(id, group, permission);
        }
        return this;
    }

    @Override
    public AccessControlList removePermission(String group, String permission) {
        Set<String> permissionsForGroup = permissionMap.get(group);
        if (permissionsForGroup != null) {
            permissionsForGroup.remove(permission);
            store.removePermission(id, group, permission);
        }
        return null;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public Serializable getId() {
        return getName();
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Map<String, Set<String>> getPermissionMap() {
        return permissionMap;
    }
}
