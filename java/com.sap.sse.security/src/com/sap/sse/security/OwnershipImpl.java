package com.sap.sse.security;

import java.util.UUID;

import com.sap.sse.security.shared.Ownership;

public class OwnershipImpl implements Ownership {    
    private final String idOfOwnedObjectAsString;
    private final String ownerUsername;
    private final UUID tenantOwnerId;
    private final String displayName;
    
    public OwnershipImpl(String idAsString, String ownerUsername, UUID tenantOwnerId, String displayName) {
        this.idOfOwnedObjectAsString = idAsString;
        this.ownerUsername = ownerUsername;
        this.tenantOwnerId = tenantOwnerId;
        this.displayName = displayName;
    }
    
    @Override
    public String getIdOfOwnedObjectAsString() {
        return idOfOwnedObjectAsString;
    }

    @Override
    public String getOwnerUsername() {
        return ownerUsername;
    }

    @Override
    public UUID getTenantOwnerId() {
        return tenantOwnerId;
    }
    
    @Override
    public String getDisplayNameOfOwnedObject() {
        return displayName;
    }
}
