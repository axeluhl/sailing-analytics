package com.sap.sailing.gwt.managementconsole.places.dashboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface DashboardResources extends ClientBundle {

    DashboardResources INSTANCE = GWT.create(DashboardResources.class);

    @Source({ ManagementConsoleResources.COLORS, "Dashboard.gss" })
    Style style();

    interface Style extends CssResource {

    }
}
