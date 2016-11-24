package com.sap.sse.security;

import java.io.Serializable;
import java.util.Set;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.WithID;

public class Tenant implements OwnedBy, NamedWithID {
    private static final long serialVersionUID = 897323675055025836L;

    /**
     * The ID for this tenant; Implements the {@link WithID} key
     */
    private final String name;
    
    private String owner;
    private Set<String> userNames;
    
    public Tenant(String name, String owner) {
        this.name = name;
        this.owner = owner;
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
    public String getOwner() {
        return owner;
    }
    
    public Tenant addUser(String name) {
        userNames.add(name);
        return this;
    }

    public Iterable<String> getUsernames() {
        return userNames;
    }
}