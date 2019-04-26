package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.AbstractOwnership;

public class Ownership extends AbstractOwnership<UserGroup, User> {
    private static final long serialVersionUID = -6379054499434958440L;

    public Ownership(User userOwner, UserGroup tenantOwner) {
        super(userOwner, tenantOwner);
    }

}
