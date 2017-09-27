package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.security.UserGroupImpl;

public class Tenant extends UserGroupImpl {
    private static final long serialVersionUID = -8831840409264252279L;

    public Tenant(UserGroup group) {
        super(group.getName(), group.getUsernames());
    }
    
    public Tenant(String name) {
        super(name);
    }
    
    public Tenant(String name, Set<String> usernames) {
        super(name, usernames);
    }
}