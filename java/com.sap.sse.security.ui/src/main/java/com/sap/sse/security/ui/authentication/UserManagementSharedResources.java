package com.sap.sse.security.ui.authentication;


public interface UserManagementSharedResources {
    
    UserManagementMainCss mainCss();

    UserManagementMediaCss mediaCss();
    
    public interface UserManagementMainCss {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
    }

    public interface UserManagementMediaCss {
    }

}