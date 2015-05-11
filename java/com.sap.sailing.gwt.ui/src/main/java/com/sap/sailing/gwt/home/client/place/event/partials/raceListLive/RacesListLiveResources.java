package com.sap.sailing.gwt.home.client.place.event.partials.raceListLive;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RacesListLiveResources extends ClientBundle {
    public static final RacesListLiveResources INSTANCE = GWT.create(RacesListLiveResources.class);

    @Source("RacesListLive.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String race();
        String race_item();
        String race_fleetcorner();
        String race_fleetcorner_icon();
        String button();
        String race_itemrank();
        String race_itemshort();
        String race_itemlong();
        String race_itemwind();
        String race_itemname();
        String race_itemwinner();
        String race_item_flag();
        String race_item_position();
        String race_item_winner();
        String race_itemcenter();
        String race_itemright();
        String race_itemright_button();
        String race_itemstatus();
        String race_itemstatus_progressbar();
        String race_itemstatus_progressbar_progress();
        String racewinners();
        String race_fleetcornerblue();
        String race_fleetcornerred();
        String race_fleetcornersilver();
        String race_fleetcornergold();
        String race_fleetcorneryellow();
        String raceslist();
        String raceslist_head();
        String raceslist_head_item();
        String raceslist_head_itemicon();
        String raceslist_head_itemflag();
        String raceslist_head_item_winner();
        String raceslist_head_itemcenter();
        String raceslist_head_itemright();
        String raceslist_head_itembutton();
        String raceslistlive();
    }
}
