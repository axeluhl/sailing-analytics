package com.sap.sailing.gwt.home.desktop.partials.eventdescription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventDescriptionResources extends ClientBundle {
    public static final EventDescriptionResources INSTANCE = GWT.create(EventDescriptionResources.class);

    @Source("EventDescription.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String box();
        String box_header();
        String box_content();
    }
}
