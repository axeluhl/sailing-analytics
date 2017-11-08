package com.sap.sailing.gwt.home.desktop.partials.seriesheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;

public interface SeriesHeaderResources extends SharedDesktopResources {
    public static final SeriesHeaderResources INSTANCE = GWT.create(SeriesHeaderResources.class);

    @Source("SeriesHeader.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String leaderboardquickaccess();
        String eventheader();
        String eventheader_breadcrumb();
        String eventheader_intro();
        String eventheader_intro_logo();
        String eventheader_intro_logo_image();
        String eventheader_intro_name();
        String eventheader_intro_details();
        String eventheader_intro_details_item();
        String eventheader_intro_details_item_inactive();
        String eventheader_intro_details_itemlink();
        String eventheader_status();
        String eventheader_status_title();
        String eventheader_status_body();
        String locationicon();
        String eventnavigation();
        String eventnavigationnormal();
        String navbar_button();
    }

}
