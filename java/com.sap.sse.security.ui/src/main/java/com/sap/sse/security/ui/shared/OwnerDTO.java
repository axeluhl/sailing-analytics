package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Owner;

public class OwnerDTO implements Owner, IsSerializable {    
    private String id;
    private String owner;
    private String tenantOwner;
    private String displayName;
    
    OwnerDTO() {} // for serialization only
    
    public OwnerDTO(String id, String owner, String tenantOwner, String displayName) {
        this.id = id;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
        this.displayName = displayName;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getOwner() {
        return owner;
    }
    
    @Override
    public String getTenantOwner() {
        return tenantOwner;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
