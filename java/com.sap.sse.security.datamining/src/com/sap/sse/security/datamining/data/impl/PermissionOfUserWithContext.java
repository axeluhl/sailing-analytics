package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasPermissionOfUserContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionOfUserWithContext extends AbstractPermissionOfUserWithContext implements HasPermissionOfUserContext {
    private final HasUserContext userContext;
    
    public PermissionOfUserWithContext(HasUserContext userContext, WildcardPermission permission) {
        super(permission);
        this.userContext = userContext;
    }

    @Override
    public HasUserContext getUser() {
        return userContext;
    }
}
