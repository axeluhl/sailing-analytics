package com.sap.sse.gwt.common;


public interface CommonSharedResources {
    
    CommonMainCss mainCss();

    CommonMediaCss mediaCss();
    
    public interface CommonMainCss {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
    }

    public interface CommonMediaCss {
    }

}