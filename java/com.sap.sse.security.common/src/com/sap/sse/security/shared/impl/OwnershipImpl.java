package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;

public class OwnershipImpl implements Ownership {    
    private static final long serialVersionUID = -6379054499434958440L;
    private SecurityUser userOwner;
    private UserGroup tenantOwner;
    
    @Deprecated
    OwnershipImpl() {} // for GWT serialization only
    
    public OwnershipImpl(SecurityUser userOwner, UserGroup tenantOwner) {
        this.userOwner = userOwner;
        this.tenantOwner = tenantOwner;
    }
    
    @Override
    public SecurityUser getUserOwner() {
        return userOwner;
    }

    @Override
    public UserGroup getTenantOwner() {
        return tenantOwner;
    }
    
    @Override
    public String toString() {
        return "OwnershipImpl [userOwner=" + userOwner + ", tenantOwner=" + tenantOwner + "]";
    }
}
