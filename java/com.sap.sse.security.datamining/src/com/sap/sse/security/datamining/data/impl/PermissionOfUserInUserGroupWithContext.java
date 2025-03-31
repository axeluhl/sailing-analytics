package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.security.datamining.data.HasPermissionOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionOfUserInUserGroupWithContext extends AbstractPermissionOfUserWithContext implements HasPermissionOfUserInUserGroupContext {
    private final HasUserInUserGroupContext userInUserGroupContext;
    
    public PermissionOfUserInUserGroupWithContext(HasUserInUserGroupContext userInUserGroupContext, WildcardPermission permission) {
        super(permission);
        this.userInUserGroupContext = userInUserGroupContext;
    }

    @Override
    public HasUserInUserGroupContext getUserInUserGroupContext() {
        return userInUserGroupContext;
    }

    @Override
    public HasUserContext getUser() {
        return getUserInUserGroupContext();
    }
}
