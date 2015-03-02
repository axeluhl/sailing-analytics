package com.sap.sailing.gwt.home.client.place.event2.ui.regattaHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegattaHeaderResources extends ClientBundle {
    public static final RegattaHeaderResources INSTANCE = GWT.create(RegattaHeaderResources.class);

    @Source("RegattaHeader.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String regattaheader();
        String regattaheaderlink();
        String regattaheader_logo();
        String regattaheader_content();
        String regattaheader_content_title();
        String regattaheader_content_details();
        String regattaheader_content_details_item();
        String regattaheader_arrow();
    }
}
