package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventSeriesOverviewResources extends ClientBundle {

    @Source({ ManagementConsoleResources.COLORS, "EventSeriesOverview.gss" })
    Style style();

    @Source("../../../resources/images/Image-EventBackdrop.png")
    ImageResource eventBackdrop();

    interface Style extends CssResource {
        @ClassName("event-series-cards")
        String eventSeriesCards();

        @ClassName("custom-teaser")
        String customTeaser();

        @ClassName("event-series-card-panel")
        String eventSeriesCardPanel();

        @ClassName("event-series-card-content")
        String eventSeriesCardContent();

        @ClassName("event-series-info")
        String eventSeriesInfo();

        @ClassName("details")
        String details();
    }
}
