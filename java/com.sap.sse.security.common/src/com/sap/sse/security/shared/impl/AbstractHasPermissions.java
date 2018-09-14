package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.Permission.DefaultModes;
import com.sap.sse.security.shared.Permission.Mode;
import com.sap.sse.security.shared.WildcardPermission;

public abstract class AbstractHasPermissions implements HasPermissions {
    private Permission permissionType;
    
    @Override
    public Permission getPermissionType() {
        return permissionType;
    }

    @Override
    public String getId() {
        return new PermissionToObjectIdConverter().getObjectIdsAsString(getPermission(DefaultModes.READ)).iterator().next();
    }

    @Override
    public WildcardPermission getPermission(Mode... operationModes) {
        return new WildcardPermission(getStringPermission(operationModes), /* case sensitive */ true);
    }

    @Override
    public String getStringPermission(Mode... operationModes) {
        return getPermissionType().getStringPermission(operationModes)+":"+getRelativeObjectId(); // FIXME escape ",", ":", and "*" in relative object ID
    }
}
