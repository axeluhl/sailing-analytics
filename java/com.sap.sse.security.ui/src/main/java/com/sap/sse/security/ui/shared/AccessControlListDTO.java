package com.sap.sse.security.ui.shared;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AccessControlListDTO implements IsSerializable {
    private String id;
    private String owner;
    
    private Map<UserGroupDTO, Set<String>> permissionMap;
    
    AccessControlListDTO() {} // for serialization only
    
    public AccessControlListDTO(String id, String owner, Map<UserGroupDTO, Set<String>> permissionMap) {
        this.id = id;
        this.owner = owner;
        this.permissionMap = permissionMap;
    }
    
    public String getId() {
        return id;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public boolean hasPermission(String user, String permission) {
        for (Map.Entry<UserGroupDTO, Set<String>> entry : permissionMap.entrySet()) {
            if (entry.getKey().contains(user) && entry.getValue().contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    public Map<UserGroupDTO, Set<String>> getPermissionMap() {
        return permissionMap;
    }
}
