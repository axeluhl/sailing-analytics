package com.sap.sse.security.shared.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.GwtIncompatible;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;

public class SecurityUserImpl implements SecurityUser {
    private static final long serialVersionUID = -3639860207453072248L;

    private String name;
    
    /**
     * Roles can refer back to this user object, e.g., for the user qualification, or during the tenant
     * qualification if this user belongs to the tenant for which the role is qualified. Therefore, this
     * set has to be transient, and the {@link #roleListForSerialization} field takes over the serialization
     * which is resolved by {@link #readResolve}.
     * 
     * @see #writeObject
     * @see #readResolve
     */
    private transient Set<Role> roles;
    
    private List<Role> roleListForSerialization;
    
    private Set<WildcardPermission> permissions;
    
    /**
     * Creates a user with empty permission set and empty role set
     */
    public SecurityUserImpl(String name) {
        this(name, new HashSet<Role>(), new HashSet<WildcardPermission>());
    }
    
    public SecurityUserImpl(String name, Iterable<Role> roles, Iterable<WildcardPermission> permissions) {
        this.name = name;
        this.roles = new HashSet<>();
        Util.addAll(roles, this.roles);
        this.permissions = new HashSet<>();
        for (WildcardPermission permission : permissions) {
            this.permissions.add(permission);
        }
    }
    
    @GwtIncompatible
    private void writeObject(ObjectOutputStream oos) throws IOException {
        roleListForSerialization = new ArrayList<>(roles);
        oos.defaultWriteObject();
        roleListForSerialization = null;
    }

    @GwtIncompatible
    protected Object readResolve() {
        roles = new HashSet<>(roleListForSerialization);
        roleListForSerialization = null;
        return this;
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
