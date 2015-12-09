package com.sap.sailing.gwt.home.shared.app;

import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public interface ClientFactoryWithSecurity {
    UserManagementServiceAsync getUserManagement();
    
    UserManagementContext getUserManagementContext();
}
