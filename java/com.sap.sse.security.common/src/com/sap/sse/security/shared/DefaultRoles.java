package com.sap.sse.security.shared;

import java.util.Collections;

public enum DefaultRoles implements AbstractRole {
    ADMIN("admin", Collections.<String>singletonList("*")); // TODO: change to *:* when we introduce the new permission system to the frontend
    
    private final String rolename;
    private final Iterable<String> permissions;
    
    private DefaultRoles(String rolename, Iterable<String> permissions) {
        this.rolename = rolename;
        this.permissions = permissions;
    }
    
    @Override
    public String getRolename() {
        return rolename;
    }
    
    

    @Override
    public Iterable<String> getPermissions() {
        return permissions;
    }
}
