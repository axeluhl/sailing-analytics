package com.sap.sailing.gwt.home.client.place.events.recent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventsOverviewRecentResources extends ClientBundle {
    public static final EventsOverviewRecentResources INSTANCE = GWT.create(EventsOverviewRecentResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/events/recent/EventsOverviewRecent.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventteasercontainer();
        String eventteaser();
        String eventteaser_name();
        String eventteaser_image();
        String eventteaser_series();
        String eventteaser_location();
        String eventsoverviewnavigation();
        String gridalternator();
        String eventsoverviewrecent();
        String eventsoverviewrecent_year();
        String eventsoverviewrecent_year_header();
        String eventsoverviewrecent_yearcollapsed();
        String eventsoverviewrecent_year_header_info_arrow_image();
        String eventsoverviewrecent_year_header_title();
        String eventsoverviewrecent_year_header_info();
        String eventsoverviewrecent_year_header_info_arrow();
        String eventsoverviewrecent_year_header_info_item();
        String eventsoverviewrecent_year_header_info_item_value();
        String eventsoverviewrecent_year_content();
    }
}
