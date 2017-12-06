package com.sap.sse.security.ui.shared;

import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Ownership;

public class OwnerDTO implements Ownership, IsSerializable {    
    private String idOfOwnedObjectAsString;
    private String ownerUsername;
    private UUID tenantOwnerId;
    private String displayNameOfOwnedObject;
    
    OwnerDTO() {} // for serialization only
    
    public OwnerDTO(String idOfOwnedObjectAsString, String ownerUsername, UUID tenantOwnerId, String displayNameOfOwnedObject) {
        this.idOfOwnedObjectAsString = idOfOwnedObjectAsString;
        this.ownerUsername = ownerUsername;
        this.tenantOwnerId = tenantOwnerId;
        this.displayNameOfOwnedObject = displayNameOfOwnedObject;
    }
    
    @Override
    public String getIdOfOwnedObjectAsString() {
        return idOfOwnedObjectAsString;
    }
    
    @Override
    public String getOwnerUsername() {
        return ownerUsername;
    }
    
    @Override
    public UUID getTenantOwnerId() {
        return tenantOwnerId;
    }

    @Override
    public String getDisplayNameOfOwnedObject() {
        return displayNameOfOwnedObject;
    }
}
