package com.sap.sse.security.ui.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.WildcardPermission;

public class RoleDTO implements Role, IsSerializable {
    private static final long serialVersionUID = 4207815227996945521L;
    
    private UUID id;
    private String name;
    private Set<String> permissions;
    
    RoleDTO() {} // for serialization only

    public RoleDTO(UUID id, String name) {
        this(id, name, new HashSet<String>());
    }

    public RoleDTO(UUID id, String name, Set<String> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
    }
    
    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<WildcardPermission> getPermissions() {
        HashSet<WildcardPermission> wildcardPermissions = new HashSet<>();
        for (String permissionString : permissions) {
            wildcardPermissions.add(new WildcardPermission(permissionString, true));
        }
        return wildcardPermissions;
    }
}
