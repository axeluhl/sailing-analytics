package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.SecurityUser;

public class OwnershipImpl implements Ownership {    
    private static final long serialVersionUID = -6379054499434958440L;
    private String idOfOwnedObjectAsString;
    private SecurityUser userOwner;
    private Tenant tenantOwner;
    private String displayNameOfOwnedObject;
    
    @Deprecated
    OwnershipImpl() {} // for GWT serialization only
    
    public OwnershipImpl(String idOfOwnedObjectAsString, SecurityUser userOwner, Tenant tenantOwner, String displayNameOfOwnedObject) {
        this.idOfOwnedObjectAsString = idOfOwnedObjectAsString;
        this.userOwner = userOwner;
        this.tenantOwner = tenantOwner;
        this.displayNameOfOwnedObject = displayNameOfOwnedObject;
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
        return displayNameOfOwnedObject;
    }

    @Override
    public String toString() {
        return "OwnershipImpl [idOfOwnedObjectAsString=" + idOfOwnedObjectAsString + ", userOwner=" + userOwner
                + ", tenantOwner=" + tenantOwner + ", displayNameOfOwnedObject=" + displayNameOfOwnedObject + "]";
    }
}
