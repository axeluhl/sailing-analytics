package com.sap.sse.security;

import com.sap.sse.security.shared.Owner;

public class OwnerImpl implements Owner {    
    private final String id;
    private final String owner;
    private final String tenantOwner;
    private final String displayName;
    
    public OwnerImpl(String id, String owner, String tenantOwner, String displayName) {
        this.id = id;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
        this.displayName = displayName;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getTenantOwner() {
        return tenantOwner;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }

    
}
