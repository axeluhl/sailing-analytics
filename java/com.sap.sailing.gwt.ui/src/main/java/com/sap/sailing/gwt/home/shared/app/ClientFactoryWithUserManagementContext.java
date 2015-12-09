package com.sap.sailing.gwt.home.shared.app;


public interface ClientFactoryWithUserManagementContext {
    UserManagementContext getUserManagementContext();
    void resetUserManagementContext();
}
