package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.common.NamedWithID;

public interface UserGroup extends NamedWithID, HasAccessControlList {
    public Set<String> getUsernames();
    
    public UserGroup add(String user);
    public UserGroup remove(String user);
    public boolean contains(String user);
}
