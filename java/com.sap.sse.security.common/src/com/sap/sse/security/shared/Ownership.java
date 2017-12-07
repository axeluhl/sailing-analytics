package com.sap.sse.security.shared;

public interface Ownership {
    String getIdOfOwnedObjectAsString();
    SecurityUser getUserOwner();
    Tenant getTenantOwner();
    String getDisplayNameOfOwnedObject();
}