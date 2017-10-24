package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoleImpl implements Role {
    private static final long serialVersionUID = 1530130914120936419L;
    
    private final UUID id;
    private final String displayName;
    private final Set<String> permissions;

    public RoleImpl(UUID id, String displayName) {
        this(id, displayName, new HashSet<String>());
    }

    public RoleImpl(UUID id, String displayName, Set<String> permissions) {
        this.id = id;
        this.displayName = displayName;
        this.permissions = permissions;
    }

    @Override
    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public Serializable getId() {
        return id;
    }
}
