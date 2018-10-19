package com.sap.sse.security.shared.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.GwtIncompatible;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;

public class UserGroupImpl implements UserGroup {
    private static final long serialVersionUID = -6387489363559803841L;

    private UUID id;
    private String name;
    
    /**
     * A {@link HashSet} such as the one used for {@link #users} must not reference back to this object, or
     * serialization / deserialization will be corrupt. In particular, during de-serialization the {@link User}
     * objects will not have been fully constructed yet when they are to be added to the {@link HashSet}, causing
     * {@link NullPointerException}s in their {@link User#hashCode} method execution. Therefore, we use a list for
     * transporting the users through serialization, but outside of this use case the field is left {@code null}.
     * 
     * @see #writeObject
     * @see #readObject
     */
    private List<SecurityUser> usersAsListForSerialization;
    private transient Set<SecurityUser> users;
    
    public UserGroupImpl(UUID id, String name) {
        this(id, name, new HashSet<SecurityUser>());
    }
    
    public UserGroupImpl(UUID id, String name, Iterable<? extends SecurityUser> users) {
        this.id = id;
        this.name = name;
        this.users = new HashSet<>();
        Util.addAll(users, this.users);
    }
    
    @GwtIncompatible
    private void writeObject(ObjectOutputStream oos) throws IOException {
        usersAsListForSerialization = new ArrayList<>(users);
        oos.defaultWriteObject();
        usersAsListForSerialization = null;
    }
    
    @GwtIncompatible
    protected Object readResolve() {
        users = new HashSet<>();
        users.addAll(usersAsListForSerialization);
        usersAsListForSerialization = null;
        return this;
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
    public void add(SecurityUser user) {
        users.add(user);
    }
    
    @Override
    public void remove(SecurityUser user) {
        users.remove(user);
    }
    
    @Override
    public boolean contains(SecurityUser user) {
        return users.contains(user);
    }

    @Override
    public Iterable<SecurityUser> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    @Override
    public String toString() {
        return "UserGroupImpl [id=" + id + ", name=" + name + ", users=" + users + "]";
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
        UserGroupImpl other = (UserGroupImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getId().toString());
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.USER_GROUP;
    }
}
