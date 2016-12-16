package com.sap.sse.security;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.WithID;

public class Tenant implements UserGroup {
    private static final long serialVersionUID = 897323675055025836L;

    /**
     * The ID for this tenant; Implements the {@link WithID} key
     */
    private final String name;
    
    private AccessControlList acl;
    private Set<String> usernames;
    
    public Tenant(String name, AccessControlList acl) {
        this.name = name;
        this.acl = acl;
        this.usernames = new HashSet<>();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Serializable getId() {
        return getName();
    }

    @Override
    public AccessControlList getAccessControlList() {
        return acl;
    }
    
    @Override
    public Tenant add(String name) {
        usernames.add(name);
        return this;
    }
    
    @Override
    public Tenant remove(String user) {
        usernames.remove(user);
        return this;
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