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
    
    @Source("com/sap/sailing/gwt/ui/client/images/DarkGreyBoats/DarkGreyDW_Port.png")
    ImageResource lowlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/DarkGreyBoats/DarkGreyDW_Starbord.png")
    ImageResource lowlightedBoatIconDW_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/YellowBoats/YellowDW_Port.png")
    ImageResource highlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/YellowBoats/YellowDW_Starbord.png")
    ImageResource highlightedBoatIconDW_Starboard();
}