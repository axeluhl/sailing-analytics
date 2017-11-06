package com.sap.sailing.gwt.home.desktop.partials.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface SharingButtonsResources extends ClientBundle {
    public static final SharingButtonsResources INSTANCE = GWT.create(SharingButtonsResources.class);

    @Source("SharingButtons.gss")
    LocalCss css();
    
    @Source("email-icon.png")
    ImageResource email();
    
    @Source("facebook-icon.png")
    ImageResource facebook();
    
    @Source("twitter-icon.png")
    ImageResource twitter();

    public interface LocalCss extends CssResource {
        String eventheader_sharing();
        String eventheader_sharing_item();
        String eventheader_sharing_itememail();
        String eventheader_sharing_itemtwitter();
        String eventheader_sharing_itemfacebook();
    }

}
