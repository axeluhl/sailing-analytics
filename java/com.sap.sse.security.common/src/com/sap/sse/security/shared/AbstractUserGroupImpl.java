package com.sap.sse.security.shared;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;

/**
 * A group of users; equality and hash code are based solely on the {@link #getId() ID}.
 */
public abstract class AbstractUserGroupImpl<U extends UserReference, RD extends RoleDefinition>
        extends SecurityUserGroupImpl<RD> {

    private static final long serialVersionUID = 5449819084645794859L;

    private Set<U> users;

    protected AbstractUserGroupImpl(UUID id, String name, Set<U> users, Map<RD, Boolean> roleDefinitionMap) {
        super(id, name, roleDefinitionMap);
        this.users = users;
    }

    public Iterable<U> getUsers() {
        return users;
    }

    public void add(U user) {
        users.add(user);
    }

    public void remove(U user) {
        users.remove(user);
    }

    public boolean contains(U user) {
        return users.contains(user);
    }

    public Boolean put(final RD roleDefinition, final boolean forAll) {
        return this.roleDefinitionMap.put(roleDefinition, forAll);
    }

    public void remove(final RD roleDefinition) {
        this.roleDefinitionMap.remove(roleDefinition);
    }

    @Override
    public String toString() {
        return "User group "+getName()+", "+Util.size(getUsers())+" users";
    }
}
