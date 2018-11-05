package com.sap.sse.security.shared;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public abstract class RolePrototype implements RoleDefinition {
    private static final long serialVersionUID = 3291793984984443193L;

    private final UUID id;
    private final String name;
    private final Set<WildcardPermission> permissions;

    protected RolePrototype(String name, String uuidAsString, Iterable<? extends HasPermissions> permissions) {
        this(name, uuidAsString, getWildcardPermissions(permissions));
    }

    private static WildcardPermission[] getWildcardPermissions(Iterable<? extends HasPermissions> permissions) {
        final WildcardPermission[] result = new WildcardPermission[Util.size(permissions)];
        int i = 0;
        for (final HasPermissions p : permissions) {
            result[i++] = p.getPermission();
        }
        return result;
    }

    protected RolePrototype(String name, String uuidAsString, WildcardPermission... permissions) {
        this.name = name;
        this.id = UUID.fromString(uuidAsString);
        this.permissions = new HashSet<>();
        for (final WildcardPermission p : permissions) {
            this.permissions.add(p);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Set<WildcardPermission> getPermissions() {
        return permissions;
    }

    @Override
    public void setName(String newName) {
        throw new UnsupportedOperationException("Cannot change the name of role " + getName());
    }

    @Override
    public void setPermissions(Iterable<WildcardPermission> permissions) {
        throw new UnsupportedOperationException("Cannot change the permissions of role " + getName());
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
        RolePrototype other = (RolePrototype) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.ROLE_DEFINITION;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getId().toString());
    }
}
