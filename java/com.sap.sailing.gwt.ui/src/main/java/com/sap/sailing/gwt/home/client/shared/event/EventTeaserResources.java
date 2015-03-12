package com.sap.sailing.gwt.home.client.shared.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface EventTeaserResources extends ClientBundle {
    public static final EventTeaserResources INSTANCE = GWT.create(EventTeaserResources.class);

    @Source("EventTeaser.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventteasercontainer();
        String eventteaser();
        String eventteaser_name();
        String eventteaser_image();
        String eventteaser_series();
        String eventteaser_title();
        String eventteaser_title_name();
        String eventteaser_title_label();
        String label();
        String eventteaser_location();
    }
    
    @Source("com/sap/sailing/gwt/home/images/default_event_photo.jpg")
    ImageResource defaultEventPhotoImage();
}
