package com.sap.sse.security.ui.shared;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RolePermissionModel;
import com.sap.sse.security.shared.WildcardPermission;

public class RolePermissionModelDTO implements RolePermissionModel, IsSerializable {
    private Map<UUID, Role> roles;
    
    RolePermissionModelDTO() {} // for serialization only
    
    public RolePermissionModelDTO(Map<UUID, Role> roles) {
        this.roles = roles;
    }
    
    @Override
    public String getName(UUID id) {
        return roles.get(id).getName();
    }
    
    @Override
    public Iterable<WildcardPermission> getPermissions(UUID id) {
        return roles.get(id).getPermissions();
    }
    
    @Override
    public Iterable<Role> getRoles() {
        return new ArrayList<Role>(roles.values());
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(UUID id, WildcardPermission permission) {
        return implies(id, permission, null);
    }
    
    @Override
    public boolean implies(UUID id, WildcardPermission permission, Owner ownership) {
        return implies(id, roles.get(id).getName(), permission, ownership);
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(UUID id, String name, WildcardPermission permission, Owner ownership) {
        String[] parts = name.split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().equals(parts[1]))) {
            for (WildcardPermission rolePermission : getPermissions(id)) {
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
