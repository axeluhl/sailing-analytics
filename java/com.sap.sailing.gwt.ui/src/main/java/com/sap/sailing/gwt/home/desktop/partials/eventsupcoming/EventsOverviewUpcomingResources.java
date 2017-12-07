package com.sap.sailing.gwt.home.desktop.partials.eventsupcoming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface EventsOverviewUpcomingResources extends ClientBundle {
    public static final EventsOverviewUpcomingResources INSTANCE = GWT.create(EventsOverviewUpcomingResources.class);

    @Source("EventsOverviewUpcoming.gss")
    LocalCss css();
    
    @Source("upcoming-events-icon.png")
    ImageResource upcomingEvents();

    public interface LocalCss extends CssResource {
        String accordion();
        String accordion_trigger();
        String accordion_content();
        String eventsoverviewupcoming();
        String accordioncollapsed();
        String eventsoverviewupcoming_header_info_arrow_image();
        String eventsoverviewupcoming_header();
        String eventsoverviewupcoming_header_icon();
        String eventsoverviewupcoming_header_title();
        String eventsoverviewupcoming_header_ticker();
        String eventsoverviewupcoming_header_tickerhide();
        String eventsoverviewupcoming_header_info();
        String eventsoverviewupcoming_header_info_arrow();
        String eventsoverviewupcoming_header_info_item();
        String eventsoverviewupcoming_header_info_item_value();
        String eventsoverviewupcoming_content();
        String eventsoverviewupcoming_content_event();
        String eventsoverviewupcoming_content_event_name();
        String eventsoverviewupcoming_content_event_location();
        String eventsoverviewupcoming_content_event_countdown();
    }
}
