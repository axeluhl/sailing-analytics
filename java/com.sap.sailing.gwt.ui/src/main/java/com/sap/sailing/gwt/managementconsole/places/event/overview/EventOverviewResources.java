package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventOverviewResources  extends ClientBundle {

    @Source({ ManagementConsoleResources.COLORS, ManagementConsoleResources.SIZES, "EventOverview.gss" })
    Style style();

    @Source("../../../resources/images/Image-EventBackdrop.png")
    ImageResource eventBackdrop();

    interface Style extends CssResource {

        @ClassName("featured-marker")
        String featuredMarker();

        String cards();

        @ClassName("card-panel")
        String cardPanel();

        @ClassName("event-card")
        String eventCard();

        String title();

        String details();

        @ClassName("event-card-container")
        String eventCardContainer();

        @ClassName("custom-teaser")
        String customTeaser();

    }
}