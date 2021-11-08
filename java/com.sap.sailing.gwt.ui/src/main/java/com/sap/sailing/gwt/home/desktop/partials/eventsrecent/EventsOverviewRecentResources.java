package com.sap.sailing.gwt.home.desktop.partials.eventsrecent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventsOverviewRecentResources extends ClientBundle {
    public static final EventsOverviewRecentResources INSTANCE = GWT.create(EventsOverviewRecentResources.class);

    @Source("EventsOverviewRecent.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventsoverviewnavigation();
        String gridalternator();
        String accordion();
        String accordion_trigger();
        String accordion_content();
        String accordion_panel();
        String eventsoverviewrecent();
        String eventsoverviewrecent_year();
        String accordioncollapsed();
        String eventsoverviewrecent_year_header_info_arrow_image();
        String eventsoverviewrecent_year_header();
        String eventsoverviewrecent_year_header_title();
        String eventsoverviewrecent_year_header_info();
        String eventsoverviewrecent_year_header_info_arrow();
        String eventsoverviewrecent_year_header_info_item();
        String eventsoverviewrecent_year_header_info_item_value();
        String eventsoverviewrecent_year_content();
        String eventsoverviewrecent_year_content_floattoinlineblock();
        String eventsoverviewrecent_year_content_floattoinlineblock_child();
    }
}
