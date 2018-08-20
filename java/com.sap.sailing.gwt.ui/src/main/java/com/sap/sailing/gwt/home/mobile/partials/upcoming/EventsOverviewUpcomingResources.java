package com.sap.sailing.gwt.home.mobile.partials.upcoming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface EventsOverviewUpcomingResources extends ClientBundle {
    public static final EventsOverviewUpcomingResources INSTANCE = GWT.create(EventsOverviewUpcomingResources.class);

    @Source("EventsOverviewUpcoming.gss")
    LocalCss css();
    
    @Source("calender-icon-blue.svg")
    @MimeType("image/svg+xml")
    DataResource calendar();

    public interface LocalCss extends CssResource {
        String accordion();
        String accordion_trigger();
        String accordion_content();
        String eventsoverviewupcoming();
        String accordioncollapsed();

        String eventsoverviewupcoming_header_info_arrow();
        String eventsoverviewupcoming_header();

        String eventsoverviewupcoming_headeritem();
        String eventsoverviewupcoming_header_icon();
        String eventsoverviewupcoming_header_title();
        String eventsoverviewupcoming_header_info();

        String eventsoverviewupcoming_header_info_text();
        String eventsoverviewupcoming_content();
        String eventsoverviewupcoming_content_event();

        String eventsoverviewupcoming_content_event_link();

        String eventsoverviewupcoming_content_event_link_body();

        String eventsoverviewupcoming_content_event_link_body_name();

        String eventsoverviewupcoming_content_event_link_body_location();

        String eventsoverviewupcoming_content_event_link_arrow();

        String eventsoverviewupcoming_content_event_link_arrow_image();
    }
}
