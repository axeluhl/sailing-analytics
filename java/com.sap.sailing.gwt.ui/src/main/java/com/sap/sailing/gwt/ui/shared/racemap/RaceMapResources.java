package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface RaceMapResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/buoy.png")
    ImageResource buoyIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/buoy_red.png")
    ImageResource buoyRedIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/buoy_yellow.png")
    ImageResource buoyYellowIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/buoy_green.png")
    ImageResource buoyGreenIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/wind_combined.png")
    ImageResource combinedWindIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/wind_expedition.png")
    ImageResource expeditionWindIcon();
}