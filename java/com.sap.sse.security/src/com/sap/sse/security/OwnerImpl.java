package com.sap.sse.security;

import java.util.UUID;

import com.sap.sse.security.shared.Owner;

public class OwnerImpl implements Owner {    
    private final String idAsString;
    private final String owner;
    private final UUID tenantOwner;
    private final String displayName;
    
    public OwnerImpl(String idAsString, String owner, UUID tenantOwner, String displayName) {
        this.idAsString = idAsString;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
        this.displayName = displayName;
    }
    
    @Override
    public String getId() {
        return idAsString;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public UUID getTenantOwner() {
        return tenantOwner;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }

    
}
