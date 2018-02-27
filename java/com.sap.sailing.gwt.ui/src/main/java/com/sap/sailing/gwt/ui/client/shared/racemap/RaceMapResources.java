package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;

public interface RaceMapResources extends ClientBundle {
    @Source("com/sap/sailing/gwt/ui/client/images/cameraBoat.png")
    ImageResource cameraBoatIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/umpireBoat.png")
    ImageResource umpireBoatIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/startBoat.png")
    ImageResource startBoatIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/wind_combined.png")
    ImageResource combinedWindIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/true_north_indicator.png")
    ImageResource trueNorthIndicatorIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/wind_expedition.png")
    ImageResource expeditionWindIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/maneuver_red.png")
    ImageResource maneuverMarkerRed();

    @Source("com/sap/sailing/gwt/ui/client/images/maneuver_green.png")
    ImageResource maneuverMarkerGreen();
    
    @Source("com/sap/sailing/gwt/ui/client/images/windfinder-logo.svg")
    TextResource getWindFinderLogo();

    @Source("RaceMap.css")
    RaceMapStyle raceMapStyle();
}