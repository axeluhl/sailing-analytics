package com.sap.sse.security.ui.shared;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.RolePermissionModel;
import com.sap.sse.security.shared.WildcardPermission;

public class RolePermissionModelDTO implements RolePermissionModel, IsSerializable {
    private Set<String> rules;
    
    RolePermissionModelDTO() {} // for serialization only
    
    public RolePermissionModelDTO(Set<String> rules) {
        this.rules = rules;
    }
    
    @Override
    public Iterable<String> getPermissions(String role) {
        return rules;
    }
    
    @Override
    public boolean implies(String role, WildcardPermission permission) { // TODO as default implementation in interface
        return implies(role, permission, null);
    }
    
    @Override
    public boolean implies(String role, WildcardPermission permission, Owner ownership) { // TODO as default implementation in interface
        String[] parts = role.split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().equals(parts[1]))) {
            for (String rolePermissionString : getPermissions(role)) {
                WildcardPermission rolePermission = new WildcardPermission(rolePermissionString, true);
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}
