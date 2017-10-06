package com.sap.sse.security;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.UserGroupImpl;

public class Tenant extends UserGroupImpl {
    private static final long serialVersionUID = -8831840409264252279L;

    public Tenant(UserGroup group) {
        super((UUID) group.getId(), group.getName(), group.getUsernames());
    }
    
    public Tenant(UUID id, String name) {
        super(id, name);
    }
    
    public Tenant(UUID id, String name, Set<String> usernames) {
        super(id, name, usernames);
    }
}