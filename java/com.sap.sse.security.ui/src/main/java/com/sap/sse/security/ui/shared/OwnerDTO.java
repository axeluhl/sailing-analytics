package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.Owner;

public class OwnerDTO implements Owner, IsSerializable {
    private static final long serialVersionUID = -1230152699506825038L;
    
    private String id;
    private String owner;
    private String tenantOwner;
    
    OwnerDTO() {} // for serialization only
    
    public OwnerDTO(String id, String owner, String tenantOwner) {
        this.id = id;
        this.owner = owner;
        this.tenantOwner = tenantOwner;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return getId();
    }
    
    @Override
    public String getOwner() {
        return owner;
    }
    
    @Override
    public String getTenantOwner() {
        return tenantOwner;
    }

    
}
