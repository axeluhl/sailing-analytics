package com.sap.sse.security.shared;

public enum DefaultPermissions implements Permission {
    ALL_EVENTS("event:*");
    
    private DefaultPermissions(String stringPermission) {
        this.stringPermission = stringPermission;
    }
    
    public String getStringPermission() {
        return stringPermission;
    }
    
    private final String stringPermission;
}
