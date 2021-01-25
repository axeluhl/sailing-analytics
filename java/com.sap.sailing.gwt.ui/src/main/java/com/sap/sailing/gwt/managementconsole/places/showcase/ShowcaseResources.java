package com.sap.sailing.gwt.managementconsole.places.showcase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface ShowcaseResources extends ClientBundle {

    ShowcaseResources INSTANCE = GWT.create(ShowcaseResources.class);

    @Source({ ManagementConsoleResources.COLORS, "Showcase.gss" })
    Style style();

    @Source("Image-EventBackdrop.png")
    ImageResource eventBackdrop();

    interface Style extends CssResource {

        String featuredMarker();

        String eventCard();

        String details();

        String eventCardContainer();

        String location();
    }
}
