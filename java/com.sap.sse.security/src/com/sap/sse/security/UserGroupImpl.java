package com.sap.sse.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.WithID;

public class UserGroupImpl implements UserGroup {
    private static final long serialVersionUID = -6387489363559803841L;

    /**
     * The ID for this tenant; Implements the {@link WithID} key
     */
    private final String name;
    private final String id;
    
    private Set<String> usernames;
        
    public UserGroupImpl(String id, String name) {
        this(id, name, new HashSet<>());
    }
    
    public UserGroupImpl(String id, String name, Set<String> usernames) {
        this.id = id;
        this.name = name;
        this.usernames = usernames;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Serializable getId() {
        return id;
    }
    
    @Override
    public void add(String name) {
        usernames.add(name);
    }
    
    @Override
    public void remove(String user) {
        usernames.remove(user);
    }
    
    @Override
    public boolean contains(String user) {
        return usernames.contains(user);
    }

    @Override
    public Set<String> getUsernames() {
        return usernames;
    }  
}
