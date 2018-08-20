package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;

public interface RegattaHeaderResources extends SharedDesktopResources {
    public static final RegattaHeaderResources INSTANCE = GWT.create(RegattaHeaderResources.class);

    @Source("RegattaHeader.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String regattaheader();
        String regattaheaderpadding();
        String regattaheader_logo();
        String regattaheader_content();
        String regattaheader_content_title();
        String regattaheader_content_details();
        String regattaheader_content_details_item();
        String regattaheader_toggle();
        String regattaheader_toggle_icon();
        String standings_listhidden();
        String regattaheader_arrow();
        String regattaheader_indicators();
        String regattaheader_indicators_next_to_arrow();
        String regattaheader_indicator_disabled();
        String regattaheader_update();
        String regattaheader_update_title();
        String regattaheader_update_title_icon();
        String regattaheader_update_timestamp();
        
        String iconHeaderGPS();
        String iconHeaderWind();
        String iconHeaderVideo();
        String iconHeaderAudio();
        
        String iconGPS();
        String iconWind();
        String iconVideo();
        String iconAudio(); 
    }
}
