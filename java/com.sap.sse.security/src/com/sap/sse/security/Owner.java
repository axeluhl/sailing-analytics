package com.sap.sse.security;

import com.sap.sse.common.NamedWithID;

public interface Owner extends NamedWithID {
    public String getOwner();
    public String getTenantOwner();
}