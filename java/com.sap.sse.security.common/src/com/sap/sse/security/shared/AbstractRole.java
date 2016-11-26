package com.sap.sse.security.shared;

public interface AbstractRole {
    String getRolename();
    
    Iterable<String> getPermissions();

    String name();

    int ordinal();
}
