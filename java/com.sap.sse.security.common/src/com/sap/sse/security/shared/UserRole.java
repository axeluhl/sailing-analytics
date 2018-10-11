package com.sap.sse.security.shared;

import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class UserRole extends RolePrototype {    
    private static final long serialVersionUID = 3291793984984443193L;
    
    private static final UserRole INSTANCE = new UserRole();
    
    UserRole() {
        super("user", "ad1d5148-b13d-4464-90c4-7c396e4d4e2e",
                new WildcardPermission(SecuredSecurityTypes.USER.getStringPermission(HasPermissions.DefaultActions.UPDATE)),
                new WildcardPermission(
                        SecuredSecurityTypes.USER.getStringPermission(HasPermissions.DefaultActions.READ)),
                new WildcardPermission("*:" + DefaultActions.CREATE.name() + ":*"));
    }
    
    public static UserRole getInstance() {
        return INSTANCE;
    }
}
