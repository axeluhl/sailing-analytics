package com.sap.sailing.gwt.home.client.place.events.recent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventsOverviewRecentResources extends ClientBundle {
    public static final EventsOverviewRecentResources INSTANCE = GWT.create(EventsOverviewRecentResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/events/recent/EventsOverviewRecent.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
    }
}
