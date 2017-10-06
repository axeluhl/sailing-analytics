package com.sap.sse.security.ui.shared;

import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Owner;

public class OwnerDTO implements Owner, IsSerializable {    
    private String idAsString;
    private String owner;
    private UUID tenantOwner;
    private String displayName;
    
    OwnerDTO() {} // for serialization only
    
    public OwnerDTO(String idAsString, String owner, UUID tenantOwner, String displayName) {
        this.idAsString = idAsString;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
        this.displayName = displayName;
    }
    
    @Override
    public String getId() {
        return idAsString;
    }
    
    @Override
    public String getOwner() {
        return owner;
    }
    
    @Override
    public UUID getTenantOwner() {
        return tenantOwner;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
