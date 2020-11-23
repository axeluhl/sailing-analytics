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

        @ClassName("featured-marker")
        String featuredMarker();
        
        String cards();

        @ClassName("card-panel")
        String cardPanel();

        @ClassName("event-card")
        String eventCard();

        String details();

        @ClassName("event-card-container")
        String eventCardContainer();

        String location();

        @ClassName("custom-teaser")
        String customTeaser();

    }
}