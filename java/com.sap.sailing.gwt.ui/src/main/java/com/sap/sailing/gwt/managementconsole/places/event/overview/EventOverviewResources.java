package com.sap.sailing.gwt.managementconsole.places.event.overview;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventOverviewResources extends ClientBundle {

    @Source({ ManagementConsoleResources.COLORS, ManagementConsoleResources.SIZES, "EventOverview.gss" })
    Style style();

    @Source("../../../resources/images/Image-EventBackdrop.png")
    ImageResource eventBackdrop();

    interface Style extends CssResource {

        @ClassName("custom-teaser")
        String customTeaser();

        @ClassName("event-cards")
        String eventCards();

        @ClassName("event-card-panel")
        String eventCardPanel();

        @ClassName("event-card-content")
        String eventCardContent();

        @ClassName("event-info")
        String eventInfo();

        @ClassName("details")
        String details();
    }
}