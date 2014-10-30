package com.sap.sailing.gwt.home.client.shared.recentevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface RecentEventResources extends ClientBundle {
    public static final RecentEventResources INSTANCE = GWT.create(RecentEventResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/recentevent/RecentEvent.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventteaser();
        String eventteaser_name();
        String eventteaser_image();
        String eventteaser_series();
        String eventteaser_location();
        String eventteasercontainer();
    }
    
    @Source("com/sap/sailing/gwt/home/images/default_event_photo.jpg")
    ImageResource defaultEventPhotoImage();
}
