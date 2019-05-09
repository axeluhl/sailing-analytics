package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconResources extends ClientBundle {
    
    public static final IconResources INSTANCE = GWT.create(IconResources.class);

    @Source("images/logo.png")
    ImageResource sapLogo();

    @Source("images/user.png")
    ImageResource user();
    
    @Source("images/user_small.png")
    ImageResource userSmall();
    
    @Source("images/user_icon.png")
    ImageResource userIcon();

    @Source("images/status_red.png")
    ImageResource statusRed();
    
    @Source("images/status_green.png")
    ImageResource statusGreen();
    
    @Source("images/status_yellow.png")
    ImageResource statusYellow();
    
    @Source("images/status_blue.png")
    ImageResource statusBlue();
    
    @Source("images/status_grey.png")
    ImageResource statusGrey();
}
