package com.sap.sse.security.shared;

public enum DefaultPermissions {
    ALL_EVENTS("event:*");
    
    private DefaultPermissions(String permissionname) {
        this.permissionname = permissionname;
    }
    
    public String getPermissionname() {
        return permissionname;
    }
    
    private final String permissionname;
}
