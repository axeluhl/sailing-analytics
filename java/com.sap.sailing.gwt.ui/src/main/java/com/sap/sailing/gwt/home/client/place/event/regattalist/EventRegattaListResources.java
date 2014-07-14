package com.sap.sailing.gwt.home.client.place.event.regattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventRegattaListResources extends ClientBundle {
    public static final EventRegattaListResources INSTANCE = GWT.create(EventRegattaListResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/regattalist/EventRegattaList.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventregattalist();
        String regatta();
        String regattacompact();
        String regatta_intro_details_itemhideoncompact();
        String regattalink();
        String regatta_intro_image();
        String regatta_intro_header_name();
        String regatta_intro();
        String regatta_intro_header();
        String regatta_intro_header_logo();
        String regatta_intro_header_labels();
        String label();
        String regatta_intro_races();
        String regatta_intro_races_race();
        String regatta_intro_meta();
        String regatta_intro_meta_scoring();
        String regatta_intro_meta_leaderboardlink();
        String regatta_intro_details();
        String regatta_intro_details_item();
        String regatta_intro_details_item_sub();
        String regatta_intro_details_item_value();
    }
}
