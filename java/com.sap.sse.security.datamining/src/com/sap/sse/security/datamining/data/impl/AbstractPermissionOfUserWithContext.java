package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasPermissionOfUserContext;
import com.sap.sse.security.shared.WildcardPermission;

public abstract class AbstractPermissionOfUserWithContext implements HasPermissionOfUserContext {
    private final WildcardPermission permission;
    
    public AbstractPermissionOfUserWithContext(WildcardPermission permission) {
        this.permission = permission;
    }

    @Override
    public WildcardPermission getPermission() {
        return permission;
    }
}
