package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

/**
 * A group of users; equality and hash code are based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractUserGroup<U extends SecurityUser<?, ?>>
        implements NamedWithID, WithQualifiedObjectIdentifier {
    private static final long serialVersionUID = 1L;

    private Set<U> users;
    private UUID id;
    private String name;

    public AbstractUserGroup(Set<U> users, UUID id, String name) {
        super();
        this.users = users;
        this.id = id;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.USER_GROUP;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(id.toString());
    }
}
