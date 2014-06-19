package com.sap.sailing.gwt.home.client.place.event.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventMediaResources extends ClientBundle {
    public static final EventMediaResources INSTANCE = GWT.create(EventMediaResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/media/EventMedia.css")
    LocalCss css();

    public interface LocalCss extends CssResource {

    }
}
