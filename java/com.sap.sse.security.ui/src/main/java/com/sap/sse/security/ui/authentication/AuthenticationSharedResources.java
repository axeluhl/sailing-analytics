package com.sap.sse.security.ui.authentication;


public interface AuthenticationSharedResources {
    
    AuthenticationMainCss mainCss();

    AuthenticationMediaCss mediaCss();
    
    public interface AuthenticationMainCss {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
    }

    public interface AuthenticationMediaCss {
    }

}