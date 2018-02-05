package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.SecurityUser;

public class OwnershipImpl implements Ownership {    
    private static final long serialVersionUID = -6379054499434958440L;
    private SecurityUser userOwner;
    private Tenant tenantOwner;
    
    @Deprecated
    OwnershipImpl() {} // for GWT serialization only
    
    public OwnershipImpl(SecurityUser userOwner, Tenant tenantOwner) {
        this.userOwner = userOwner;
        this.tenantOwner = tenantOwner;
    }
    
    @Override
    public SecurityUser getUserOwner() {
        return userOwner;
    }

    @Override
    public Tenant getTenantOwner() {
        return tenantOwner;
    }
    
    @Override
    public String toString() {
        return "OwnershipImpl [userOwner=" + userOwner + ", tenantOwner=" + tenantOwner + "]";
    }
}
