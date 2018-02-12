package com.sap.sailing.gwt.ui.server;

import com.sap.sailing.domain.common.security.Permission;
import com.sap.sse.security.shared.RolePrototype;

public class AdminConsoleRole extends RolePrototype {
    private static final long serialVersionUID = 3291793984984443193L;
    
    private static final AdminConsoleRole INSTANCE = new AdminConsoleRole();
    
    AdminConsoleRole() {
        super("adminconsole", "74651f7e-2407-4e75-9480-a6900f924ce0",
            Permission.getAdminConsolePermissions());
    }
    
    public static AdminConsoleRole getInstance() {
        return INSTANCE;
    }
}
