package com.sap.sailing.gwt.home.client.place.event.regattaleaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventRegattaLeaderboardResources extends ClientBundle {
    public static final EventRegattaLeaderboardResources INSTANCE = GWT.create(EventRegattaLeaderboardResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/regattaleaderboard/EventRegattaLeaderboard.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String leaderboardcompetitor();
        String leaderboardcompetitor_rank();
        String leaderboardcompetitor_image();
        String leaderboardcompetitor_nationality();
        String leaderboardcompetitor_itemcenter();
        String eventregattaleaderboard_table();
        String eventregattaleaderboard();
        String eventregattaleaderboard_meta();
        String eventregattaleaderboard_meta_title();
        String eventregattaleaderboard_meta_update();
        String eventregattaleaderboard_meta_button();
        String eventregattaleaderboard_meta_reload();
        String eventregattaleaderboard_meta_settings();
        String eventregattaleaderboard_header();
        String eventregattaleaderboard_header_item();
        String eventregattaleaderboard_header_itemrank();
        String eventregattaleaderboard_header_itemcompetitor();
        String eventregattaleaderboard_header_itemname();
        String eventregattaleaderboard_header_itemdata();
        String eventregattaleaderboard_header_itemdata_element();
        String eventregattaleaderboard_header_itemdata_elementascending();
        String eventregattaleaderboard_header_itemdata_elementdescending();
        String eventregattaleaderboard_header_itempoints();
        String eventregattaleaderboard_header_item_name();
        String eventregattaleaderboard_header_itemdata_element_name();
        String eventregattaleaderboard_header_itemdescending();
        String eventregattaleaderboard_header_itemascending();
        String eventregattaleaderboard_header_table();
        String eventregattaleaderboard_body();
        String eventregattaleaderboard_data_element();
        String leaderboardcompetitor_item();
        String leaderboardcompetitor_itemcentercenter();
        String leaderboardcompetitor_item_table();
        String leaderboardcompetitor_itemcenter_table();
        String leaderboardcompetitorshort();
    }
}
