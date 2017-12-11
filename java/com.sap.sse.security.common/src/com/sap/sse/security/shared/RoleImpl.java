package com.sap.sse.security.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;

/**
 * Equality ({@link #equals(Object)} and {@link #hashCode()} are based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RoleImpl implements Role {
    private static final long serialVersionUID = -402472324567793082L;
    
    private UUID id;
    private String name;
    private Set<WildcardPermission> permissions;

    @Deprecated
    RoleImpl() {} // for GWT serialization only
    
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoleImpl other = (RoleImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name + " (permissions: " + permissions + ")";
    }
}
