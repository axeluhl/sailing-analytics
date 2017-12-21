package com.sap.sse.security.shared;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserRole implements RoleDefinition {    
    private static final long serialVersionUID = 3291793984984443193L;
    
    private static final UserRole INSTANCE = new UserRole();
    private static final String NAME = "user";
    private static final String UUID_STRING = "ad1d5148-b13d-4464-90c4-7c396e4d4e2e";
    private UUID id;
    private Set<WildcardPermission> permissions;
    
    UserRole() {
        id = UUID.fromString(UUID_STRING);
        permissions = new HashSet<>();
        permissions.add(new WildcardPermission("user:edit:*"));
        permissions.add(new WildcardPermission("user:view:*"));
    }
    
    @Override
    public String getName() {
        return NAME;
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
        throw new UnsupportedOperationException("Cannot change the name of role "+getName());
    }

    @Override
    public void setPermissions(Iterable<WildcardPermission> permissions) {
        throw new UnsupportedOperationException("Cannot change the permissions of role "+getName());
    }

    public static UserRole getInstance() {
        return INSTANCE;
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
        UserRole other = (UserRole) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
