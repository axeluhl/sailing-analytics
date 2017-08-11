package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class OwnerDTO implements IsSerializable {
    private String id;
    private String owner;
    private String tenantOwner;
    
    OwnerDTO() {} // for serialization only
    
    public OwnerDTO(String id, String owner, String tenantOwner) {
        this.id = id;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
    }
    
    public String getId() {
        return id;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public String getTenantOwner() {
        return tenantOwner;
    }
}
