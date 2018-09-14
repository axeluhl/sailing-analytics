package com.sap.sse.security.shared;

import com.sap.sse.security.shared.impl.DefaultPermissions;

public class UserRole extends RolePrototype {    
    private static final long serialVersionUID = 3291793984984443193L;
    
    private static final UserRole INSTANCE = new UserRole();
    
    UserRole() {
        super("user", "ad1d5148-b13d-4464-90c4-7c396e4d4e2e",
                new WildcardPermission(DefaultPermissions.USER.getStringPermission(Permission.DefaultModes.UPDATE), /* case sensitive */ true),
                new WildcardPermission(DefaultPermissions.USER.getStringPermission(Permission.DefaultModes.READ), /* case sensitive */ true));
    }
    
    public static UserRole getInstance() {
        return INSTANCE;
    }
}
