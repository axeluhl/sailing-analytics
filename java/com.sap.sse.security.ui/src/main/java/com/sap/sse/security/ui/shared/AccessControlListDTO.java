package com.sap.sse.security.ui.shared;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Permission;

public class AccessControlListDTO implements IsSerializable {
    private String id;
    private TenantDTO owner;
    
    private Map<UserGroupDTO, Set<Permission>> permissionMap;
    
    AccessControlListDTO() {} // for serialization only
    
    public AccessControlListDTO(String id, TenantDTO owner, Map<UserGroupDTO, Set<Permission>> permissionMap) {
        this.id = id;
        this.owner = owner;
        this.permissionMap = permissionMap;
    }
    
    public String getId() {
        return id;
    }
    
    public TenantDTO getOwner() {
        return owner;
    }
    
    public boolean hasPermission(String user, Permission permission) {
        for (Map.Entry<UserGroupDTO, Set<Permission>> entry : permissionMap.entrySet()) {
            if (entry.getKey().contains(user) && entry.getValue().contains(permission)) {
                return true;
            }
        }
        return false;
    }
}
