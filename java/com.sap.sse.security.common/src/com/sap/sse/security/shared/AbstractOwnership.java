package com.sap.sse.security.shared;

import java.io.Serializable;

public abstract class AbstractOwnership<G extends SecurityUserGroup<?>, U extends UserReference>
        implements Serializable {
    private static final long serialVersionUID = 1L;

    private U userOwner;
    private G tenantOwner;

    public AbstractOwnership(U userOwner, G tenantOwner) {
        this.userOwner = userOwner;
        this.tenantOwner = tenantOwner;
    }

    public U getUserOwner() {
        return userOwner;
    }

    public G getTenantOwner() {
        return tenantOwner;
    }

    @Override
    public String toString() {
        return "AbstractOwnership [userOwner=" + userOwner + ", tenantOwner=" + tenantOwner + "]";
    }
}
