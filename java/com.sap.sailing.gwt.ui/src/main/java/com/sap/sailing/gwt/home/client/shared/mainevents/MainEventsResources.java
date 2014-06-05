package com.sap.sailing.gwt.home.client.shared.mainevents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MainEventsResources extends ClientBundle {
    public static final MainEventsResources INSTANCE = GWT.create(MainEventsResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/mainevents/MainEvents.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String mainevents();    
        String eventteaser();
        String eventteaser_name();
        String eventteaser_image();
        String eventteaser_series();
        String eventteaser_location();
    }
}
