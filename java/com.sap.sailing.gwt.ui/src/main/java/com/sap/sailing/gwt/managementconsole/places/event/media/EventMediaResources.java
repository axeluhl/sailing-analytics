package com.sap.sailing.gwt.managementconsole.places.event.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventMediaResources extends ClientBundle {

    EventMediaResources INSTANCE = GWT.create(EventMediaResources.class);

    @Source({ ManagementConsoleResources.COLORS, "EventMedia.gss" })
    Style style();

    interface Style extends CssResource {

    }
}
