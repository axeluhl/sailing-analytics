package com.sap.sse.gwt.theme.client.component.recentevent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface RecentEventResources extends ClientBundle {
    public static final RecentEventResources INSTANCE = GWT.create(RecentEventResources.class);

    @Source("RecentEvent.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventteaser();
        String eventteaser_name();
        String eventteaser_image();
        String eventteaser_series();
        String eventteaser_location();
        String eventteasercontainer();
    }

    @Source("default_event_photo.jpg")
    ImageResource defaultEventPhotoImage();
}
