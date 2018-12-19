package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

/**
 * A group of users; equality and hash code are based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractUserGroupImpl<U extends UserReference>
        extends SecurityUserGroupImpl {
    private static final long serialVersionUID = 1L;

    private Set<U> users;

    public AbstractUserGroupImpl(Set<U> users, UUID id, String name) {
        super(id, name);
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
}
