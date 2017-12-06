package com.sap.sse.security;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.UserGroup;

public class UserGroupImpl implements UserGroup {
    private static final long serialVersionUID = -6387489363559803841L;

    private final UUID id;
    private final String name;
    
    private Set<String> usernames;
        
    public UserGroupImpl(UUID id, String name) {
        this(id, name, new HashSet<>());
    }
    
    public UserGroupImpl(UUID id, String name, Set<String> usernames) {
        this.id = id;
        this.name = name;
        this.usernames = usernames;
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
