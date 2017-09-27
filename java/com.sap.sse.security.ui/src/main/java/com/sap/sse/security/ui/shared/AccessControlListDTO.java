package com.sap.sse.security.ui.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.AccessControlList;

public class AccessControlListDTO implements AccessControlList, IsSerializable {
    private static final long serialVersionUID = -6425803762946910564L;

    private String id;
    
    private Map<UserGroupDTO, Set<String>> permissionMap;
    
    AccessControlListDTO() {} // for serialization only
    
    public AccessControlListDTO(String id, Map<UserGroupDTO, Set<String>> permissionMap) {
        this.id = id;
        this.permissionMap = permissionMap;
    }
    
    @Override
    public String getName() {
        return getId();
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public boolean hasPermission(String user, String permission) {
        for (Map.Entry<UserGroupDTO, Set<String>> entry : permissionMap.entrySet()) {
            if (entry.getKey().contains(user) && entry.getValue().contains(permission)) {
                return true;
            }
        }
        return false;
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
