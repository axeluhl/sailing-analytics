package com.sap.sse.security.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;

public class RoleImpl implements Role {
    private static final long serialVersionUID = -402472324567793082L;
    
    private UUID id;
    private String name;
    private Set<WildcardPermission> permissions;

    public RoleImpl(UUID id, String name) {
        this(id, name, new HashSet<WildcardPermission>());
    }

    public RoleImpl(UUID id, String name, Iterable<WildcardPermission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = new HashSet<>();
        Util.addAll(permissions, this.permissions);
    }
    
    @Override
    public UUID getId() {
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

    @Override
    public String toString() {
        return "RoleImpl [id=" + id + ", name=" + name + ", permissions=" + permissions + "]";
    }
}
