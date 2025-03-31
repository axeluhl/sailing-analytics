package com.sap.sse.security;

import com.sap.sse.security.shared.WildcardPermission;

public class ShiroWildcardPermissionFromParts extends org.apache.shiro.authz.permission.WildcardPermission {
    private static final long serialVersionUID = -6361446629960026098L;

    public ShiroWildcardPermissionFromParts(WildcardPermission permission) {
        super();
        setParts(permission.getParts());
    }
}