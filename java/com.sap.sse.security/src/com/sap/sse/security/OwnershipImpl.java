package com.sap.sse.security;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.SecurityUser;

public class OwnershipImpl implements Ownership {    
    private static final long serialVersionUID = -6379054499434958440L;
    private String idOfOwnedObjectAsString;
    private SecurityUser userOwner;
    private Tenant tenantOwner;
    private String displayName;
    
    @Deprecated
    OwnershipImpl() {} // for GWT serialization only
    
    public OwnershipImpl(String idAsString, SecurityUser userOwner, Tenant tenantOwner, String displayName) {
        this.idOfOwnedObjectAsString = idAsString;
        this.userOwner = userOwner;
        this.tenantOwner = tenantOwner;
        this.displayName = displayName;
    }
    
    @Override
    public String getIdOfOwnedObjectAsString() {
        return idOfOwnedObjectAsString;
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
    public String getDisplayNameOfOwnedObject() {
        return displayName;
    }

    @Override
    public String toString() {
        return "OwnershipImpl [idOfOwnedObjectAsString=" + idOfOwnedObjectAsString + ", userOwner=" + userOwner
                + ", tenantOwner=" + tenantOwner + ", displayName=" + displayName + "]";
    }
}
