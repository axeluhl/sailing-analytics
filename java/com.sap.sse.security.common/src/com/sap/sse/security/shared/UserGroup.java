package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;

public interface UserGroup extends NamedWithID {
    public Set<SecurityUser> getUsers();
    
    public void add(SecurityUser user);
    public void remove(SecurityUser user);
    public boolean contains(SecurityUser user);

    @Override
    UUID getId();
}
