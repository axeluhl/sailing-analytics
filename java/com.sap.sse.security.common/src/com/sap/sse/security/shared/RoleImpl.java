package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoleImpl implements Role {
    private static final long serialVersionUID = -402472324567793082L;
    
    private UUID id;
    private String name;
    private Set<WildcardPermission> permissions;

    public RoleImpl(UUID id, String name) {
        this(id, name, new HashSet<WildcardPermission>());
    }

    public RoleImpl(UUID id, String name, Set<WildcardPermission> permissions) {
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
        return Collections.unmodifiableSet(permissions);
    }
}
