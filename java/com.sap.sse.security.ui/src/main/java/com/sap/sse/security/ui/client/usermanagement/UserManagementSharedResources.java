package com.sap.sse.security.ui.client.usermanagement;


public interface UserManagementSharedResources {
    
    UserManagementMainCss mainCss();

    UserManagementMediaCss mediaCss();
    
    public interface UserManagementMainCss {
        String button();
        String buttonprimaryoutlined();
    }

    public interface UserManagementMediaCss {
    }

}