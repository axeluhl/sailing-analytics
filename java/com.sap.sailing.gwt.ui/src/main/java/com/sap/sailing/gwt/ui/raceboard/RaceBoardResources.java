package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.generic.resource.AuthenticationResources;


public interface RaceBoardResources extends CommonSharedResources, AuthenticationResources {
    public static final RaceBoardResources INSTANCE = GWT.create(RaceBoardResources.class);
    
    @Source({CommonSharedResources.RESET, CommonSharedResources.MAIN, "raceboard-main.gss"})
    RaceBoardMainCss mainCss();
    
    @Source({CommonSharedResources.MEDIA, "raceboard-media.gss"})
    RaceBoardMediaCss mediaCss();
    
    //TODO image is stored in wrong folder
    @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
    ImageResource editIcon();

    
    public interface RaceBoardMainCss extends CommonMainCss {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
        
        String usermanagement_icon();
        String usermanagement_loggedin();
        String usermanagement_view();
        String usermanagement_view_content_wrapper();
        String usermanagement_view_content();
        String usermanagement_mobile();
        String usermanagement_open();
    }

    public interface RaceBoardMediaCss extends CommonMediaCss {
    }

}