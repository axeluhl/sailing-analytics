package com.sap.sse.security;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.SecurityUser;

public class OwnershipImpl implements Ownership {    
    private final String idOfOwnedObjectAsString;
    private final SecurityUser userOwner;
    private final Tenant tenantOwner;
    private final String displayName;
    
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
