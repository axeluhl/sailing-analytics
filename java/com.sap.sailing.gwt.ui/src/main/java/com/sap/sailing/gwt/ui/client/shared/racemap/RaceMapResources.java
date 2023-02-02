package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
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

    @Source("com/sap/sailing/gwt/ui/client/images/uploadVideoButton.png")
    @MimeType("image/png")
    DataResource uploadVideoIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/compass.png")
    @MimeType("image/png")
    DataResource compass();

    @Source("com/sap/sailing/gwt/ui/client/images/minus.svg")
    @MimeType("image/svg+xml")
    DataResource minusIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/plus.svg")
    @MimeType("image/svg+xml")
    DataResource plusIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/more.png")
    @MimeType("image/png")
    DataResource moreIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/settings.png")
    @MimeType("image/png")
    DataResource settingsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/fullscreen.svg")
    @MimeType("image/svg+xml")
    DataResource fullScreen();

    @Source("com/sap/sailing/gwt/ui/client/images/exitFullscreen.svg")
    @MimeType("image/svg+xml")
    DataResource exitFullScreen();

    @Source("RaceMap.css")
    RaceMapStyle raceMapStyle();
    
    @Source("com/sap/sailing/gwt/common/client/premium/icon_premium.svg")
    @MimeType("image/svg+xml")
    DataResource premiumIcon();
}