package com.sap.sse.gwt.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;


public interface CommonSharedResources extends ClientBundle {
    public static final CommonSharedResources INSTANCE = GWT.create(CommonSharedResources.class);
    
    public static final String RESET = "com/sap/sse/gwt/common/common-reset.gss";
    public static final String MAIN = "com/sap/sse/gwt/common/common-main.gss";
    public static final String MEDIA = "com/sap/sse/gwt/common/common-media.gss";
    
    @Source({RESET, MAIN})
    CommonMainCss mainCss();

    @Source(MEDIA)
    CommonMediaCss mediaCss();
    
    public interface CommonMainCss extends CssResource {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
    }

    public interface CommonMediaCss extends CssResource {
    }

}