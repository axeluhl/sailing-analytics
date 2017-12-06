package com.sap.sse.security.shared;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;

public interface UserGroup extends NamedWithID {
    public Set<String> getUsernames();
    
    public void add(String user);
    public void remove(String user);
    public boolean contains(String user);

    @Override
    UUID getId();
}
