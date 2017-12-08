package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;

public class SecurityUserImpl implements SecurityUser {
    private static final long serialVersionUID = -3639860207453072248L;
    private String name;
    private Set<Role> roles;
    private Set<WildcardPermission> permissions;
    private Tenant defaultTenant;

    // For GWT serialization only
    @Deprecated
    protected SecurityUserImpl() {
    }
    
    /**
     * Creates a user with empty permission set and empty role set
     */
    public SecurityUserImpl(String name, Tenant defaultTenant) {
        this(name, /* roles */ new HashSet<Role>(), defaultTenant, /* permissions */ new HashSet<WildcardPermission>());
    }
    
    public SecurityUserImpl(String name, Iterable<Role> roles, Tenant defaultTenant, Iterable<WildcardPermission> permissions) {
        this.name = name;
        this.roles = new HashSet<>();
        Util.addAll(roles, this.roles);
        this.defaultTenant = defaultTenant;
        this.permissions = new HashSet<>();
        for (WildcardPermission permission : permissions) {
            this.permissions.add(permission);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Serializable getId() {
        return getName();
    }

    @Override
    public Tenant getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(Tenant newDefaultTenant) {
        this.defaultTenant = newDefaultTenant;
    }
    
    @Override
    public Iterable<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
    
    @Override
    public Iterable<WildcardPermission> getPermissions() {
        return permissions;
    }
    
    @Override
    public boolean hasPermission(WildcardPermission permission) {
        return permissions.contains(permission);
    }
    
    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public void addPermission(WildcardPermission permission) {
        permissions.add(permission);
    }
    
    public void removePermission(WildcardPermission permission) {
        permissions.remove(permission);
    }
}
