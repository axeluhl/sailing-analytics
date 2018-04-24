package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface ManeuverTablePanelResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/shared/racemap/maneuver/ManeuverTablePanel.css")
    Style css();

    @Source("com/sap/sailing/gwt/ui/client/images/important-message.png")
    ImageResource importantMessage();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Settings.png")
    ImageResource settingsButton();

    public interface Style extends CssResource {

        String maneuverPanel();

        String contentContainer();

        String maneuverTable();

        String importantMessage();

        String settingsButton();

    }

}
