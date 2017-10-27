package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoleImpl implements Role {
    private final UUID id;
    private final String displayName;
    private final Set<WildcardPermission> permissions;

    public RoleImpl(UUID id, String displayName) {
        this(id, displayName, new HashSet<WildcardPermission>());
    }

    public RoleImpl(UUID id, String displayName, Set<WildcardPermission> permissions) {
        this.id = id;
        this.displayName = displayName;
        this.permissions = permissions;
    }

    @Override
    public Set<WildcardPermission> getPermissions() {
        return permissions;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Serializable getId() {
        return id;
    }
}
