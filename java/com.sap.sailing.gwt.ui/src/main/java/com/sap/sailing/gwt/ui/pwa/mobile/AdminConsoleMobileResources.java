package com.sap.sailing.gwt.ui.pwa.mobile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface AdminConsoleMobileResources extends ClientBundle {
    public static final AdminConsoleMobileResources INSTANCE = GWT.create(AdminConsoleMobileResources.class);

    @Source("AdminConsoleMobile.gss")
    LocalCss css();

    @Source("Image-BackdropGeneralSized.png")
    ImageResource backdropGeneralSized();
    
    @Source("Image-EventBackdrop.png")
    ImageResource eventBackdrop();
    
    @Source("Icon-NavGlobal.svg")
    @MimeType("image/svg+xml")
    DataResource navGlobal();

    public interface LocalCss extends CssResource {

        String primaryButton();

        String secondaryButton();
        
        String secondaryCtaButton();
        
        String featuredMarker();
        
        String eventCard();
        
        String details();
        
        String eventCardContainer();
        
        String location();
    }
}
