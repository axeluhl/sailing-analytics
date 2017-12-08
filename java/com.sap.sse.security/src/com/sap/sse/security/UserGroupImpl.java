package com.sap.sse.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;

public class UserGroupImpl implements UserGroup {
    private static final long serialVersionUID = -6387489363559803841L;

    private final UUID id;
    private final String name;
    
    private Set<SecurityUser> users;
        
    public UserGroupImpl(UUID id, String name) {
        this(id, name, new HashSet<>());
    }
    
    public UserGroupImpl(UUID id, String name, Iterable<? extends SecurityUser> users) {
        this.id = id;
        this.name = name;
        this.users = new HashSet<>();
        Util.addAll(users, this.users);
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
}
