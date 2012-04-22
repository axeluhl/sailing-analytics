package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface RaceMapResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/coursemark.png")
    ImageResource buoyIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/wind_combined.png")
    ImageResource combinedWindIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/wind_expedition.png")
    ImageResource expeditionWindIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Port.png")
    ImageResource lowlightedBoatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Starbord.png")
    ImageResource lowlightedBoatIcon_Starboard();
 
    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Port.png")
    ImageResource lowlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Starbord.png")
    ImageResource lowlightedBoatIconDW_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Port_Selected.png")
    ImageResource highlightedBoatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Starbord_Selected.png")
    ImageResource highlightedBoatIcon_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Port_Selected.png")
    ImageResource highlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Starbord_Selected.png")
    ImageResource highlightedBoatIconDW_Starboard();
}