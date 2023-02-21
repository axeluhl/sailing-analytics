package com.sap.sse.security.datamining.data.impl;

import com.sap.sse.common.Util;
import com.sap.sse.security.datamining.data.HasPermissionsOfUserInUserGroupContext;
import com.sap.sse.security.datamining.data.HasUserInUserGroupContext;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionOfUserInUserGroupWithContext implements HasPermissionsOfUserInUserGroupContext {
    private final HasUserInUserGroupContext userInUserGroupContext;
    private final WildcardPermission permission;
    
    public PermissionOfUserInUserGroupWithContext(HasUserInUserGroupContext userInUserGroupContext, WildcardPermission permission) {
        this.userInUserGroupContext = userInUserGroupContext;
        this.permission = permission;
    }

    @Override
    public HasUserInUserGroupContext getUserInUserGroupContext() {
        return userInUserGroupContext;
    }

    private WildcardPermission getPermission() {
        return permission;
    }

    @Override
    public String getPermissionString() {
        return getPermission().toString();
    }

    @Override
    public String getPermissionTypes() {
        return !getPermission().getParts().isEmpty() ? Util.joinStrings(",", getPermission().getParts().get(0)) : "";
    }

    @Override
    public String getPermissionActions() {
        return getPermission().getParts().size() > 1 ? Util.joinStrings(",", getPermission().getParts().get(1)) : "";
    }

    @Override
    public String getPermissionObjects() {
        return getPermission().getParts().size() > 2 ? Util.joinStrings(",", getPermission().getParts().get(2)) : "";
    }
}
