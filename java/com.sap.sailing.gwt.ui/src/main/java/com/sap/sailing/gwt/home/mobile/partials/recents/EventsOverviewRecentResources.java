package com.sap.sailing.gwt.home.mobile.partials.recents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

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
        String eventteasercontainer();
        String eventteaser();
        String eventteaser_title();
        String eventteaser_title_name();
        String eventteaser_image();
        String eventteaser_series();
        String eventteaser_name();
        String eventteaser_title_name_label();
        String label();
        String eventteaser_location();
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
        String eventsoverviewrecent_year_contenthidden();
        String eventsoverviewrecent_event();
        String eventsoverviewrecent_event_image();
        String eventteaser_title_name_state();
        String eventteaser_title_location();
        String eventsoverviewrecent_event_arrow();
        String eventsoverviewrecent_event_arrow_image();
        String togglecontainerhidden();
    }

    @Source("com/sap/sailing/gwt/home/images/default_event_photo.jpg")
    ImageResource defaultEventPhotoImage();
}
