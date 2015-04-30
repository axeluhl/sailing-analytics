package com.sap.sailing.gwt.home.client.place.event.partials.eventregatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventRegattaListResources extends ClientBundle {
    public static final EventRegattaListResources INSTANCE = GWT.create(EventRegattaListResources.class);

    @Source("EventRegattaList.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String item();
        String selectedItem();
        String eventregattalist();
        String eventregattalist_navigation();

        String regattaGroupsNavigationPanel();
    }
}
