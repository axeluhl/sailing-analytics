package com.sap.sailing.gwt.home.desktop.partials.eventheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;

public interface EventHeaderResources extends SharedDesktopResources {
    public static final EventHeaderResources INSTANCE = GWT.create(EventHeaderResources.class);

    @Source("EventHeader.gss")
    LocalCss css();
    
    @Source("location-icon.png")
    ImageResource location();

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
