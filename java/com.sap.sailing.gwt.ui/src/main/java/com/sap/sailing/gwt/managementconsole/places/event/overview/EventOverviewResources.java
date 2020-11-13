package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcaseResources;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventOverviewResources  extends ClientBundle {

    ShowcaseResources INSTANCE = GWT.create(ShowcaseResources.class);

    @Source({ ManagementConsoleResources.COLORS, "EventOverview.gss" })
    Style style();

    @Source("Image-EventBackdrop.png")
    ImageResource eventBackdrop();

    interface Style extends CssResource {

        String featuredMarker();

        String eventCard();

        String details();

        String eventCardContainer();

        String location();
        
        String customTeaser();
    }
}