package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sap.sse.gwt.common.CommonSharedResources;


public interface RaceBoardResources extends CommonSharedResources, ClientBundle {
    public static final RaceBoardResources INSTANCE = GWT.create(RaceBoardResources.class);
    
    @Source("raceboard-main.gss")
    RaceBoardMainCss mainCss();
    
    @Source("raceboard-media.gss")
    RaceBoardMediaCss mediaCss();
    
    public interface RaceBoardMainCss extends CommonMainCss, CssResource {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
    }

    public interface RaceBoardMediaCss extends CommonMediaCss, CssResource {
    }

}