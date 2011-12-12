package com.sap.sailing.gwt.ui.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface AdminConsoleResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/remove.png")
    ImageResource removeIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
    ImageResource editIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/link.png")
    ImageResource linkIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/link_break.png")
    ImageResource unlinkIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/open.png")
    ImageResource openBrowserIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/dice.png")
    ImageResource scoresIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/coursemark.png")
    ImageResource buoyIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/DarkGreyBoats/DarkGrey_Port.png")
    ImageResource lowlightedBoatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/DarkGreyBoats/DarkGrey_Starbord.png")
    ImageResource lowlightedBoatIcon_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/DarkGreyBoats/DarkGreyDW_Port.png")
    ImageResource lowlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/DarkGreyBoats/DarkGreyDW_Starbord.png")
    ImageResource lowlightedBoatIconDW_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/LightGreyBoats/LightGrey_Port.png")
    ImageResource boatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/LightGreyBoats/LightGrey_Starbord.png")
    ImageResource boatIcon_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/LightGreyBoats/LightGreyDW_Port.png")
    ImageResource boatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/LightGreyBoats/LightGreyDW_Starbord.png")
    ImageResource boatIconDW_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/YellowBoats/Yellow_Port.png")
    ImageResource highlightedBoatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/YellowBoats/Yellow_Starbord.png")
    ImageResource highlightedBoatIcon_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/YellowBoats/YellowDW_Port.png")
    ImageResource highlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/YellowBoats/YellowDW_Starbord.png")
    ImageResource highlightedBoatIconDW_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/StartFinishBoat/start_finishboat.png")
    ImageResource startFinishBoat();
}