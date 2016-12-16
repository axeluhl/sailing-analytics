package com.sap.sse.security.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.sap.sse.security.AccessControlList;
import com.sap.sse.security.AccessControlListStore;
import com.sap.sse.security.Tenant;
import com.sap.sse.security.User;
import com.sap.sse.security.UserGroup;
import com.sap.sse.security.shared.Permission;

public class AccessControlListWithStore implements AccessControlList {
    private static final long serialVersionUID = -5709064967680495227L;
    
    private String id;
    private Tenant owner;
    
    private AccessControlListStore store;
    
    private Map<UserGroup, Set<Permission>> permissionMap;
    
    public AccessControlListWithStore(String id, Tenant owner, AccessControlListStore store) {
        this.id = id;
        this.owner = owner;
        this.store = store;
    }
    
    @Override
    public boolean hasPermission(User user, Permission permission) {
        for (Map.Entry<UserGroup, Set<Permission>> entry : permissionMap.entrySet()) {
            if (entry.getKey().contains(user.getName()) && entry.getValue().contains(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AccessControlList putPermissions(UserGroup group, Set<Permission> permissions) {
        permissionMap.put(group,  permissions);
        store.putPermissions(id, group, permissions);
        return this;
    }

    @Override
    public AccessControlList addPermission(UserGroup group, Permission permission) {
        Set<Permission> permissionsForGroup = permissionMap.get(group);
        if (permissionsForGroup != null) {
            permissionsForGroup.add(permission);
            store.addPermission(id, group, permission);
        }
        return this;
    }

    @Override
    public AccessControlList removePermission(UserGroup group, Permission permission) {
        Set<Permission> permissionsForGroup = permissionMap.get(group);
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
    public Tenant getOwner() {
        return owner;
    }

    @Override
    public Map<UserGroup, Set<Permission>> getPermissionMap() {
        return permissionMap;
    }
}
