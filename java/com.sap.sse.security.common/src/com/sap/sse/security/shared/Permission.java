package com.sap.sse.security.shared;

public interface Permission {
    String name();
    
    int ordinal();
    
    String getStringPermission();
}
