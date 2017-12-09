package com.sap.sse.security.shared;

import java.io.Serializable;

public interface Ownership extends Serializable {
    String getIdOfOwnedObjectAsString();
    SecurityUser getUserOwner();
    Tenant getTenantOwner();
    String getDisplayNameOfOwnedObject();
}