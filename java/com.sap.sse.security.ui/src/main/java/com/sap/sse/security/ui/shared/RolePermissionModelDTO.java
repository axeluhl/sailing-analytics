package com.sap.sse.security.ui.shared;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.RolePermissionModel;

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
}
