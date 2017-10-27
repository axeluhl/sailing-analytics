package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdminRole implements Role {
    private static final long serialVersionUID = 902382706671071325L;
    
    private static AdminRole INSTANCE;
    private static final String NAME = "admin";
    private static final String UUID_STRING = "dc77e3d1-d405-435e-8699-ce7245f6fd7a";
    private final UUID id;
    private final Set<String> permissions;
    
    
    AdminRole() {
        id = UUID.fromString(UUID_STRING);
        permissions = new HashSet<>();
        permissions.add("*");
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public Set<String> getPermissions() {
        return permissions;
    }

    public static AdminRole getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdminRole();
        }
        return INSTANCE;
    }
}
