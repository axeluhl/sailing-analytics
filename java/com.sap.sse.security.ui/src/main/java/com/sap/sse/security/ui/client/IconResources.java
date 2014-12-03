package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconResources extends ClientBundle {
    
    public static final IconResources INSTANCE = GWT.create(IconResources.class);

    @Source("com/sap/sse/security/ui/images/logo.png")
    ImageResource sapLogo();

    @Source("com/sap/sse/security/ui/images/user.png")
    ImageResource user();
    
    @Source("com/sap/sse/security/ui/images/user_small.png")
    ImageResource userSmall();
    
    @Source("com/sap/sse/security/ui/images/user_icon.png")
    ImageResource userIcon();

    @Source("com/sap/sse/security/ui/images/status_red.png")
    ImageResource statusRed();
    
    @Source("com/sap/sse/security/ui/images/status_green.png")
    ImageResource statusGreen();
    
    @Source("com/sap/sse/security/ui/images/status_yellow.png")
    ImageResource statusYellow();
    
    @Source("com/sap/sse/security/ui/images/status_blue.png")
    ImageResource statusBlue();
    
    @Source("com/sap/sse/security/ui/images/status_grey.png")
    ImageResource statusGrey();
    
    @Source("com/sap/sse/security/ui/images/delete.png")
    ImageResource delete();
    
    @Source("com/sap/sse/gwt/client/images/remove.png")
    ImageResource remove();
}
