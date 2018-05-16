package com.sap.sse.security.shared.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;

public class SecurityUserImpl implements SecurityUser {
    private static final long serialVersionUID = -3639860207453072248L;
    private String name;
    private Set<Role> roles;
    private Set<WildcardPermission> permissions;
    
    /**
     * The tenant to use as {@link Ownership#getTenantOwner() tenant owner} of new objects created by this user
     */
    private UserGroup defaultTenant;

    // For GWT serialization only
    @Deprecated
    protected SecurityUserImpl() {
    }
    
    /**
     * Creates a user with empty permission set and empty role set
     */
    public SecurityUserImpl(String name, UserGroup defaultTenant) {
        this(name, /* roles */ new HashSet<Role>(), defaultTenant, /* permissions */ new HashSet<WildcardPermission>());
    }
    
    public SecurityUserImpl(String name, Iterable<Role> roles, UserGroup defaultTenant, Iterable<WildcardPermission> permissions) {
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
    public UserGroup getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(UserGroup newDefaultTenant) {
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
        return hasPermission(permission, /* ownership */ null);
    }
    
    @Override
    public boolean hasPermission(WildcardPermission permission, Ownership ownership) {
        return hasPermission(permission, ownership, getUserGroups(), /* ACL */ null);
    }

    @Override
    public boolean hasPermission(WildcardPermission permission, Ownership ownership,
            Iterable<UserGroup> groupsThisUserIsPartOf, AccessControlList acl) {
        return PermissionChecker.isPermitted(permission, this, groupsThisUserIsPartOf, ownership, acl);
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

    @Override
    public String toString() {
        return name+" (roles: "+getRoles()+")";
    }

    /**
     * This default implementation does not know where to obtain this user's groups from. It
     * therefore returns an empty collection.
     */
    @Override
    public Iterable<UserGroup> getUserGroups() {
        return Collections.emptyList();
    }
}
