package com.sap.sailing.gwt.home.client.place.event2.partials.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface SharingButtonsResources extends ClientBundle {
    public static final SharingButtonsResources INSTANCE = GWT.create(SharingButtonsResources.class);

    @Source("SharingButtons.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventheader_sharing();
        String eventheader_sharing_item();
        String eventheader_sharing_itememail();
        String eventheader_sharing_itemtwitter();
        String eventheader_sharing_itemfacebook();
    }

    @Source("com/sap/sailing/gwt/home/images/default_event_logo.jpg")
    ImageResource defaultEventLogoImage();

}
