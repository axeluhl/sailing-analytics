package com.sap.sse.security;

import com.sap.sse.security.shared.Owner;

public class OwnerImpl implements Owner {
    private static final long serialVersionUID = -3997304159898514597L;
    
    private final String id;
    private final String owner;
    private final String tenantOwner;
    
    public OwnerImpl(String id, String owner, String tenantOwner) {
        this.id = id;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return getId();
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getTenantOwner() {
        return tenantOwner;
    }

    
}
