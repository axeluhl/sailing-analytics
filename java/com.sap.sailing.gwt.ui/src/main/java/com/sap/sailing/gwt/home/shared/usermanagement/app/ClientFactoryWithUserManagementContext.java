package com.sap.sailing.gwt.home.shared.usermanagement.app;




public interface ClientFactoryWithUserManagementContext {
    UserManagementContext getUserManagementContext();
    void resetUserManagementContext();
}
