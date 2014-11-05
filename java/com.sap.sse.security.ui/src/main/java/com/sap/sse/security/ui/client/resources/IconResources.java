package com.sap.sse.security.ui.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconResources extends ClientBundle {
    
    public static final IconResources INSTANCE = GWT.create(IconResources.class);

    @Source("../../images/status_red.png")
    public ImageResource statusRed();
    
    @Source("../../images/status_green.png")
    public ImageResource statusGreen();
    
    @Source("../../images/status_yellow.png")
    public ImageResource statusYellow();
    
    @Source("../../images/status_blue.png")
    public ImageResource statusBlue();
    
    @Source("../../images/status_grey.png")
    public ImageResource statusGrey();
    
    @Source("../../images/delete.png")
    public ImageResource delete();
    
    @Source("com/sap/sse/gwt/client/images/remove.png")
    public ImageResource remove();
}
