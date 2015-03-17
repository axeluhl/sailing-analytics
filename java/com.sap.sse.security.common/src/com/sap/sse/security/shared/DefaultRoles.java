package com.sap.sse.security.shared;

public enum DefaultRoles implements Role {
    ADMIN("admin");
    
    private DefaultRoles(String rolename) {
        this.rolename = rolename;
    }
    
    @Override
    public String getRolename() {
        return rolename;
    }
    
    private final String rolename;
}
