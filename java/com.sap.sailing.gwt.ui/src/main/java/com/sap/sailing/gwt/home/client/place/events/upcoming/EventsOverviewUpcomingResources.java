package com.sap.sailing.gwt.home.client.place.events.upcoming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventsOverviewUpcomingResources extends ClientBundle {
    public static final EventsOverviewUpcomingResources INSTANCE = GWT.create(EventsOverviewUpcomingResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/events/upcoming/EventsOverviewUpcoming.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventsoverviewupcoming();
        String eventsoverviewupcoming_teaser();
        String eventslist();
        String eventslist_header();
        String eventslist_header_item();
        String eventslist_header_itemseries();
        String eventslist_event();
        String eventslist_event_itemname();
        String eventslist_event_link();
        String eventslist_event_item();
        String eventslist_event_itemseries();
    }
}
