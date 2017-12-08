package com.sap.sse.security.shared;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdminRole implements Role {    
    private static final long serialVersionUID = 3291793984984443193L;
    
    private static AdminRole INSTANCE;
    private static final String NAME = "admin";
    private static final String UUID_STRING = "dc77e3d1-d405-435e-8699-ce7245f6fd7a";
    private UUID id;
    private Set<WildcardPermission> permissions;
    
    AdminRole() {
        id = UUID.fromString(UUID_STRING);
        permissions = new HashSet<>();
        permissions.add(new WildcardPermission(WildcardPermission.WILDCARD_TOKEN));
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Set<WildcardPermission> getPermissions() {
        return permissions;
    }

    public static AdminRole getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdminRole();
        }
        return INSTANCE;
    }
}
