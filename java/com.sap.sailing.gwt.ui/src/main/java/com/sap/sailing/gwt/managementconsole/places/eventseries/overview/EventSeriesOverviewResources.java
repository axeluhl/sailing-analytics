package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventSeriesOverviewResources extends ClientBundle {

    EventSeriesOverviewResources INSTANCE = GWT.create(EventSeriesOverviewResources.class);

    @Source({ ManagementConsoleResources.COLORS, "EventSeriesOverview.gss" })
    Style style();

    interface Style extends CssResource {

    }
}
