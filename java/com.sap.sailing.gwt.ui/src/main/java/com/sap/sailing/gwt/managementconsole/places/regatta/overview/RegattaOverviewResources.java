package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface RegattaOverviewResources extends ClientBundle {

    @Source({ ManagementConsoleResources.COLORS, "RegattaOverview.gss" })
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

        String details();

        @ClassName("event-card-container")
        String eventCardContainer();

        String location();

        @ClassName("custom-teaser")
        String customTeaser();

        @ClassName("boatclass-logo")
        String boatclassLogo();
        
        @ClassName("races")
        String races();

    }
}