package com.sap.sailing.gwt.home.desktop.partials.mainevents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MainEventsResources extends ClientBundle {
    public static final MainEventsResources INSTANCE = GWT.create(MainEventsResources.class);

    @Source("MainEvents.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String mainevents();    
    }
}
