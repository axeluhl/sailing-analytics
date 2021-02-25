package com.sap.sailing.gwt.managementconsole.places.regatta.overview;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface RegattaOverviewResources extends ClientBundle {

    @Source({ ManagementConsoleResources.COLORS, ManagementConsoleResources.SIZES, "RegattaOverview.gss" })
    Style style();

    @Source("../../../resources/images/Image-EventBackdrop.png")
    ImageResource eventBackdrop();

    interface Style extends CssResource {

        @ClassName("boatclass-logo")
        String boatclassLogo();
        
        @ClassName("races")
        String races();

    }
}