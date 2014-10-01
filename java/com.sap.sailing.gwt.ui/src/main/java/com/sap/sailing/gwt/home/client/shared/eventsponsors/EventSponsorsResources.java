package com.sap.sailing.gwt.home.client.shared.eventsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventSponsorsResources extends ClientBundle {
    public static final EventSponsorsResources INSTANCE = GWT.create(EventSponsorsResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/eventsponsors/EventSponsors.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventsponsors();
        String eventsponsors_title();
        String eventsponsors_sponsor();
        String eventsponsors_sponsor_link();
        String eventsponsors_sponsor_link_image();
    }
}
