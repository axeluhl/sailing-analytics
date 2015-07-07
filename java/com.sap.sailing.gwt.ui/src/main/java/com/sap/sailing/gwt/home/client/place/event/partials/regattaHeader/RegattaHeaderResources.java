package com.sap.sailing.gwt.home.client.place.event.partials.regattaHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegattaHeaderResources extends ClientBundle {
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
        String regattaheader_update();
        String regattaheader_update_title();
        String regattaheader_update_title_icon();
        String regattaheader_update_timestamp();
    }
}
