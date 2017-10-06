package com.sap.sse.security.shared;

import java.util.UUID;

import com.sap.sse.common.WithID;

public interface Owner extends WithID {
    public String getOwner();
    public UUID getTenantOwner();
    public String getDisplayName();
}