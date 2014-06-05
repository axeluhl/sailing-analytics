package com.sap.sailing.gwt.home.client.shared.eventsoverview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventsOverviewResources extends ClientBundle {
    public static final EventsOverviewResources INSTANCE = GWT.create(EventsOverviewResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/eventsoverview/EventsOverview.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventteaser();
        String eventteaser_name();
        String eventteaser_image();
        String Microsoft();
        String eventteaser_series();
        String eventteaser_location();
        String eventsoverview();
        String eventsoverview_navigation();
        String eventsoverview_navigation_button();
        String eventsoverview_contenthidden();
        String eventsoverview_contentrecent();
        String eventsoverview_contentrecent_year();
        String eventsoverview_contentupcoming();
        String eventsoverview_contentupcoming_teaser();
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
