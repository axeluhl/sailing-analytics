package com.sap.sse.security.shared;

import java.util.UUID;

public interface Ownership {
    String getIdOfOwnedObjectAsString();
    String getOwnerUsername();
    UUID getTenantOwnerId();
    String getDisplayNameOfOwnedObject();
}