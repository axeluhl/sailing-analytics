package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventSeriesEventsResources extends ClientBundle {

    EventSeriesEventsResources INSTANCE = GWT.create(EventSeriesEventsResources.class);

    @Source({ ManagementConsoleResources.COLORS, "EventSeriesEvents.gss" })
    Style style();

    interface Style extends CssResource {

    }
}
