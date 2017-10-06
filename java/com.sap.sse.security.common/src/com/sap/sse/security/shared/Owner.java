package com.sap.sse.security.shared;

import com.sap.sse.common.WithID;

public interface Owner extends WithID {
    public String getOwner();
    public String getTenantOwner();
    public String getDisplayName();
}